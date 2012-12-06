package project.cs.netinfservice.netinf.node.search;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.node.search.SearchController;
import netinf.node.search.SearchService;
import netinf.node.search.impl.events.SearchServiceResultEvent;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import project.cs.netinfservice.application.MainNetInfActivity;
import project.cs.netinfservice.application.MainNetInfApplication;
import project.cs.netinfservice.database.DatabaseException;
import project.cs.netinfservice.database.IODatabase;
import project.cs.netinfservice.database.IODatabaseFactory;
import project.cs.netinfservice.util.IdentifierBuilder;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Enables search for a URL in the local SQLite DB and remote NRS.
 * @author Linus Sunde
 * @author Thiago Costa Porto
 */
public class UrlSearchService implements SearchService {

    /** Log Tag. */
    private static final String TAG = "UrlSearchService";

    /** Key for accessing the NRS IP. */
    private static final String PREF_KEY_NRS_IP = "pref_key_nrs_ip";
    /** Key for accessing the NRS Port. */
    private static final String PREF_KEY_NRS_PORT = "pref_key_nrs_port";
    /** Message ID random value max. */
    public static final int MSG_ID_MAX = 100000000;

    /** HTTP Scheme. */
    private static final String HTTP = "http://";
    /** NRS IP address. **/
    private String mDefaultHost;
    /** NRS port. **/
    private String mDefaultPort;

    /** DatamodelFactory used to create identifiers. */
    private DatamodelFactory mDatamodelFactory;
    /** The identity of a certain instance of this class. */
    private SearchServiceIdentityObject mIdentityObject;
    /** The local SQLite DB. */
    private IODatabase mDatabase;
    /** Random number generator used to create message IDs. */
    private final Random mRandomGenerator = new Random();
    /** HTTP Client. **/
    private HttpClient mClient;

    /**
     * Creates a new instance of this class.
     * @param host
     *      Default NRS Address
     * @param port
     *      Default NRS Port
     * @param datamodelFactory
     *      The DatamodelFactory to use when creating identifiers
     * @param databaseFactory
     *      The IODatabaseFactory to use when accessing the local SQLite DB
     */
    @Inject
    public UrlSearchService(
            @Named("nrs.http.host") String host,
            @Named("nrs.http.port") int port,
            @Named("nrs.http.search.timeout") int timeout,
            final DatamodelFactory datamodelFactory,
            IODatabaseFactory databaseFactory) {
        mDefaultHost = host;
        mDefaultPort = Integer.toString(port);
        mDatamodelFactory = datamodelFactory;
        mDatabase = databaseFactory.create(MainNetInfApplication.getAppContext());

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);
        mClient = new DefaultHttpClient(params);
    }

    /**
     * Searches for a URL.
     * @param type
     *      Should be DefinedQueryTemplate.URL
     * @param urls
     *      A list of exactly one element containing the URL to search for
     * @param searchId
     *      The id of the search
     * @param searchIdentity
     *      The identity object of the search service
     * @param searchController
     *      The controller that initiated the search
     */
    @Override
    public void getByQueryTemplate(String type, List<String> urls, int searchId,
            SearchServiceIdentityObject searchIdentity, SearchController searchController) {
        // TODO Make sure metadata is correctly added

        String url = urls.get(0);

        // Search in the database
        try {
            Set<Identifier> results = searchDatabase(url);
            Log.d(TAG, "Result from database:");
            for (Identifier result : results)
                logIdentifierContent(result);
            // If we found something, don't ask the NRS
            searchController.handleSearchEvent(new SearchServiceResultEvent(
                    "search result of " + getIdentityObject().getName(),
                    searchId,
                    searchIdentity,
                    results));
            return;
        } catch (DatabaseException e) {
            Log.w(TAG, e.getMessage() != null ? e.getMessage() : "Search in local db failed");
        }

        // Search in the NRS
        Set<Identifier> results = new HashSet<Identifier>();

        try {
            HttpPost search = createSearch(url);
            HttpResponse response = mClient.execute(search);
            results = handleResponse(response);
            Log.d(TAG, "Result from NRS");
            for (Identifier result : results)
                logIdentifierContent(result);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage() != null ? e.getMessage() : "Search in NRS failed");
        }

        searchController.handleSearchEvent(new SearchServiceResultEvent(
                "search result of " + getIdentityObject().getName(),
                searchId,
                searchIdentity,
                results));
    }

    /**
     * Reads the search results from a HttpResponse.
     * @param response
     *      The HttpResponse
     * @return
     *      A set of the identifiers in the search result
     * @throws Exception
     *      In case extracting the search result failed
     */
    private Set<Identifier> handleResponse(HttpResponse response)
            throws Exception {

        Set<Identifier> resultSet = new HashSet<Identifier>();

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            // 200 returns a JSON
            String jsonString = EntityUtils.toString(response.getEntity());
            JSONObject json = (JSONObject) JSONValue.parseWithException(jsonString);
            JSONArray results = (JSONArray) json.get("results");

            for (Object result : results) {
                JSONObject jsonResult = (JSONObject) result;

                // TODO: Un-hardcode this.
                String hash = getHashFromResults((String) jsonResult.get("ni"));
                String hashAlg = getHashAlgFromResults((String) jsonResult.get("ni"));
                String meta = getMetadataFromResults((JSONObject) jsonResult.get("meta"));

                Identifier identifier =
                        new IdentifierBuilder(mDatamodelFactory)
                .setHash(hash)
                .setHashAlg(hashAlg)
                .setMetadata(meta)
                .build();

                resultSet.add(identifier);
            }
        }

        return resultSet;
    }

    private void logIdentifierContent(Identifier identifier) {
        Log.d(TAG, "logIdentifierContent()");
        List<IdentifierLabel> labels = identifier.getIdentifierLabels();
        for (IdentifierLabel label : labels) {
            Log.d(TAG, "Label: " + label.getLabelName());
            Log.d(TAG, "Content: " + label.getLabelValue());
        }
    }

    /**
     * Gets hash algorithm type from the ni field in the JSON result from NRS.
     * @param ni The ni field value
     * @return A string with the Hash algorithm
     */
    private String getHashAlgFromResults(String ni) {
        int end = ni.indexOf(";");
        String substring = ni.substring(0, end);
        int start = substring.lastIndexOf("/");
        return substring.substring(start+1);
    }

    /**
     * Gets hash from the ni field in the JSON result from NRS.
     * @param ni The ni field value
     * @return A string with the Hash
     */
    private String getHashFromResults(String ni) {
        int start = ni.indexOf(";");
        return ni.substring(start+1);
    }

    /**
     * Gets the metadata from the metadata field in the JSON result from NRS.
     * @param meta The metadata field value
     * @return String with the metadata from the metadata field
     */
    private String getMetadataFromResults(JSONObject meta) {
        return meta.toString();
    }

    /**
     * Function that sends a search request to the database.
     * @param url
     *      URL to be searched
     * @return
     *      A set of identifiers with the result from the database search
     * @throws DatabaseException
     *      In case search fails
     */
    private Set<Identifier> searchDatabase(String url) throws DatabaseException {

        SearchResult searchResult = mDatabase.searchIO(url);

        Identifier identifier =
                new IdentifierBuilder(mDatamodelFactory)
        .setHash(searchResult.getHash())
        .setHashAlg(searchResult.getHashAlgorithm())
        .setMetadata(searchResult.getMetaData().convertToString())
        .build();

        Set<Identifier> resultSet = new HashSet<Identifier>();
        resultSet.add(identifier);
        return resultSet;

    }

    /**
     * Creates the search request that is going to be sent to the NRS.
     * @param url
     *      URL that is going to be searched for
     * @return
     *      HTTP Post representing the search request
     * @throws UnsupportedEncodingException
     *      In case UTF-8 is not supported
     */
    private HttpPost createSearch(String url) throws UnsupportedEncodingException {
        Log.d(TAG, "createSearch()");
        Log.d(TAG, "Creating search to send to " + HTTP + getHost() + ":" + getPort());

        // POST
        String uri = HTTP + getHost() + ":" + getPort() + "/netinfproto/search";
        HttpPost post = new HttpPost(uri);

        // URI
        StringBuilder query = new StringBuilder();
        query.append("?msgid=");
        query.append(Integer.toString(mRandomGenerator.nextInt(MSG_ID_MAX)));
        query.append("&tokens=");
        query.append(URLEncoder.encode(url, "UTF-8"));
        query.append("&ext=");
        query.append("empty");

        // Logs URI
        Log.d(TAG, "createSearch() uri: " + uri);
        Log.d(TAG, "createSearch() query: " + query.toString());

        // Encode the URL
        String encodeUrl = query.toString();
        //encodeUrl = URLEncoder.encode(query.toString(), "UTF-8");

        // Create new entity
        HttpEntity newEntity =
                new InputStreamEntity(
                        new ByteArrayInputStream(encodeUrl.getBytes()),
                        encodeUrl.getBytes().length);

        // Add header
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");

        // set post entity
        post.setEntity(newEntity);

        return post;
    }

    /**
     * Get the NRS Address.
     * @return the IP Address of the NRS
     */
    private String getHost() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(MainNetInfActivity.getActivity());
        return sharedPreferences.getString(PREF_KEY_NRS_IP, mDefaultHost);

    }

    /**
     * Get the NRS port.
     * @return the port of the NRS
     */
    private int getPort() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(MainNetInfActivity.getActivity());
        return Integer.parseInt(sharedPreferences.getString(PREF_KEY_NRS_PORT, mDefaultPort));

    }

    @Override
    public void getBySPARQL(String query, int searchId,
            SearchServiceIdentityObject searchIdentity, SearchController searchController) {
        // NOT SUPPORTED
        searchController.handleSearchEvent(new SearchServiceResultEvent(
                "search result of " + getIdentityObject().getName(),
                searchId,
                searchIdentity,
                new HashSet<Identifier>()));
    }

    // TODO Make sure we can always return true here. Is the DB really ready?
    // We think it should be
    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public SearchServiceIdentityObject getIdentityObject() {
        if (mIdentityObject == null) {
            mIdentityObject = createIdentityObject();
        }
        return mIdentityObject;
    }

    /**
     * Creates a new IdentityObject.
     * @return
     *      The created IdentityObject
     */
    private SearchServiceIdentityObject createIdentityObject() {
        SearchServiceIdentityObject idO = mDatamodelFactory.createSearchServiceIdentityObject();
        idO.setIdentifier(mDatamodelFactory.createIdentifier());
        idO.setName("SearchServiceSQLLite");
        idO.setDescription("This Search Service can be used to search in the local SQLLite DB.");
        return idO;
    }

    @Override
    public String describe() {
        return "the local SQLite DB";
    }

}
