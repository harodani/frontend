/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */
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
 * 
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
    
    /** NetInf HTTP timeout */
    private int mTimeout;

    /**
     * Creates a new instance of this class.
     * 
     * @param host
     *      Default NRS Address from property file
     * @param port
     *      Default NRS Port from property file
     * @param timeout,
     *      Default NRS timeout from property file
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
        // Initialize private variables
        mDefaultHost = host;
        mDefaultPort = Integer.toString(port);
        mDatamodelFactory = datamodelFactory;

        // Grabs Android's SQLite database
        mDatabase = databaseFactory.create(MainNetInfApplication.getAppContext());
        mTimeout = timeout;
    }

    /**
     * Searches for a URL.
     * 
     * @param type
     *      Should be DefinedQueryTemplate.URL
     * @param urls
     *      A list of exactly one element containing the URL to search for
     * @param searchId
     *      The id of the current search
     * @param searchIdentity
     *      The identity object of the search service
     * @param searchController
     *      The controller that initiated the search
     */
    @Override
    public void getByQueryTemplate(String type, List<String> urls, int searchId,
            SearchServiceIdentityObject searchIdentity, SearchController searchController) {
        // Loads url from list of urls with exactly one url
        String url = urls.get(0);

        Log.e(TAG, "Searching for url: " + url);
        
        // Search in the database
        try {
            // Do database search
            Set<Identifier> results = searchDatabase(url);
            Log.d(TAG, "Search found the url in database");

            // Send search results to search controller 
            searchController.handleSearchEvent(new SearchServiceResultEvent(
                    "search result of " + getIdentityObject().getName(),
                    searchId,
                    searchIdentity,
                    results));

            // If we found something, don't ask the NRS.
            return;
        } catch (DatabaseException e) {
            Log.e(TAG, "Search in local db didn't find anything or failed: " + e.getClass());
        }

        // Search in the NRS
        Set<Identifier> results = new HashSet<Identifier>();

        try {
            // HTTP Params
            HttpParams params = new BasicHttpParams();
            
            // Sets timeout accordingly
            HttpConnectionParams.setConnectionTimeout(params, mTimeout);
            HttpConnectionParams.setSoTimeout(params, mTimeout);
            
            // Create a new HTTP Client
            HttpClient client = new DefaultHttpClient(params);
            
            // Creates a HTTP post for search
            Log.e(TAG, "x");
            HttpPost search = createSearch(url);

            Log.e(TAG, "y");
            // Executes HTTP post
            HttpResponse response = client.execute(search);

            Log.e(TAG, "z");
            // Handles HTTP response
            results = handleResponse(response);

            Log.d(TAG, "Search found the url in the NRS");
        } catch (Exception e) {
            Log.e(TAG, "Search in NRS didn't find anything or failed: " + e.getClass());
        }

        // This sends the search results to the search controller, covering two scenarios:
        //  1 Failed search in database and failed search in NRS
        //  2 Failed search in database and successful search in NRS
        searchController.handleSearchEvent(new SearchServiceResultEvent(
                "search result of " + getIdentityObject().getName(),
                searchId,
                searchIdentity,
                results));
    }

    /**
     * Reads the search results from a HttpResponse.
     * 
     * @param response
     *      The HttpResponse
     * @return
     *      A set of the identifiers in the search result
     * @throws Exception
     *      In case extracting the search result failed
     */
    private Set<Identifier> handleResponse(HttpResponse response)
            throws Exception {
        // Make a new set of identifiers for handling the results
        Set<Identifier> resultSet = new HashSet<Identifier>();

        // Get status code from HTTP response
        int statusCode = response.getStatusLine().getStatusCode();

        // If the status code is '200' or HTTP OK
        if (statusCode == HttpStatus.SC_OK) {
            // Get the JSON returned (200 always returns a JSON)
            String jsonString = EntityUtils.toString(response.getEntity());

            // Parse String to JSON Object
            JSONObject json = (JSONObject) JSONValue.parseWithException(jsonString);

            // Go inside the results
            JSONArray results = (JSONArray) json.get("results");

            // Iterate through the results from the NRS search 
            for (Object result : results) {
                JSONObject jsonResult = (JSONObject) result;

                // Get the 'ni' field and the metadata from Results
                String niField = (String) jsonResult.get("ni");
                JSONObject metaField = (JSONObject) jsonResult.get("meta"); 

                // Extract HASH, HASH ALGORITHM and METADATA from the search results
                String hash = getHashFromResults(niField);
                String hashAlg = getHashAlgFromResults(niField);
                String meta = getMetadataFromResults(metaField);

                // Create a new Identifier with the information extracted 
                Identifier identifier =
                        new IdentifierBuilder(mDatamodelFactory)
                .setHash(hash)
                .setHashAlg(hashAlg)
                .setMetadata(meta)
                .build();

                // Add result to the set
                resultSet.add(identifier);
            }
        }

        // Return set with all results retrieved from the NRS
        return resultSet;
    }

    /**
     * Gets hash algorithm type from the ni field in the JSON result from NRS.
     * 
     * @param ni
     *      The ni field value
     * @return
     *      A string with the Hash algorithm
     */
    private String getHashAlgFromResults(String ni) {
        // ni://hash_alg;HASH
        int end = ni.indexOf(";");

        // Cut the original string
        String substring = ni.substring(0, end);

        // Walk in the new string
        int start = substring.lastIndexOf("/");

        // Return the 'hash_alg'
        return substring.substring(start+1);
    }

    /**
     * Gets hash from the ni field in the JSON result from NRS.
     * 
     * @param ni
     *      The ni field value
     * @return
     *      A string with the Hash
     */
    private String getHashFromResults(String ni) {
        // ni://hash_alg;HASH
        int start = ni.indexOf(";");
        return ni.substring(start+1);
    }

    /**
     * Gets the metadata from the metadata field in the JSON result from NRS.
     * 
     * @param meta
     *      The metadata field value
     * @return
     *      String with the metadata from the metadata field
     */
    private String getMetadataFromResults(JSONObject meta) {
        return meta.toString();
    }

    /**
     * Function that sends a search request to the database.
     * 
     * @param url
     *      URL to be searched
     * @return
     *      A set of identifiers with the result from the database search
     * @throws DatabaseException
     *      In case search fails
     */
    private Set<Identifier> searchDatabase(String url) throws DatabaseException {
        // Make a database query for the URL
        SearchResult searchResult = mDatabase.searchIO(url);

        // Create a new Identifier with the results 
        Identifier identifier =
                new IdentifierBuilder(mDatamodelFactory)
        .setHash(searchResult.getHash())
        .setHashAlg(searchResult.getHashAlgorithm())
        .setMetadata(searchResult.getMetaData().convertToString())
        .build();

        // We must return a set of identifiers
        Set<Identifier> resultSet = new HashSet<Identifier>();

        // Add the Identifier created above to the set
        resultSet.add(identifier);

        // Return the set of identifiers
        return resultSet;
    }

    /**
     * Creates the search request that is going to be sent to the NRS.
     * 
     * @param url
     *      URL that is going to be searched for
     * @return
     *      HTTP Post representing the search request
     * @throws UnsupportedEncodingException
     *      In case UTF-8 is not supported
     */
    private HttpPost createSearch(String url) throws UnsupportedEncodingException {
        // Create URI to look like http://host:port/netinfproto/search
        String uri = HTTP + getHost() + ":" + getPort() + "/netinfproto/search";

        // Create the HTTP Post object with the uri from above
        HttpPost post = new HttpPost(uri);

        // Build additional URI
        StringBuilder query = new StringBuilder();

        // Add msgId
        query.append("?msgid=");
        query.append(Integer.toString(mRandomGenerator.nextInt(MSG_ID_MAX)));

        // Add tokens
        query.append("&tokens=");
        query.append(URLEncoder.encode(url, "UTF-8")); // URL must be encoded to work

        // Add ext
        query.append("&ext=");
        query.append("empty");

        // Put the full url in its own object. Should look like this:
        // http://host:port/netinfproto/search/?msgid=MSGID&tokens=TOKENS&ext=EXT
        String fullUrl = query.toString();

        // Create new entity
        HttpEntity newEntity = new InputStreamEntity(
                new ByteArrayInputStream(fullUrl.getBytes()),
                fullUrl.getBytes().length);

        // Add header
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");

        // set post entity
        post.setEntity(newEntity);

        // return HTTP POST object with search
        return post;
    }

    /**
     * Get the NRS Address.
     * 
     * @return
     *      The IP Address of the NRS
     */
    private String getHost() {
        // Get shared preferences from Android phone
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(MainNetInfActivity.getActivity());

        // Returns NRS IP. If the IP is not stored into the shared preferences,
        // the function returns mDefaultHost, which is the NRS IP stored in this class.
        return sharedPreferences.getString(PREF_KEY_NRS_IP, mDefaultHost);
    }

    /**
     * Get the NRS port.
     * 
     * @return
     *      The port of the NRS
     */
    private int getPort() {
        // Get shared preferences from Android phone
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(MainNetInfActivity.getActivity());
        
        // Returns NRS PORT. If the PORT is not stored into the shared preferences,
        // the function returns mDefaultPort, which is the NRS PORT stored in this class.
        return Integer.parseInt(sharedPreferences.getString(PREF_KEY_NRS_PORT, mDefaultPort));
    }

    /**
     * Function not supported but required to be implemented to comply with the Search Service
     * interface. We do not implement the RDF database (our project runs on SQLite), thus turning
     * SPARQL irrelevant.
     *
     * In theory, this function should do a SPARQL query to a RDF database.
     *
     * @param query
     *      Query to be supported
     * @param searchId
     *      Id of the search
     * @param searchIdentity
     *      The identity object of the search service
     * @param searchController
     *      The controller that initiated the search
     *
     */
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

    /**
     * Returns if the search service is ready to perform a search request.
     * 
     * @return
     *      {@value}true Always, as a search can always be executed. If no service is working
     *                   (NRS or database), then it fails, but we should not prevent the search
     *                   request from happening.  
     */
    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * Gets the Search Service Identity Object associated with this Search Service.
     * 
     * @return
     *      Identity Object associated with this Search Service.
     */
    @Override
    public SearchServiceIdentityObject getIdentityObject() {
        // Identity Object should always exist. If there isnt one, create a new one.
        if (mIdentityObject == null) {
            mIdentityObject = createIdentityObject();
        }
        
        // Return the Identity Object associated with this Search Service
        return mIdentityObject;
    }

    /**
     * Creates a new IdentityObject.
     * 
     * @return
     *      The created IdentityObject
     */
    private SearchServiceIdentityObject createIdentityObject() {
        // Creates a new Search Service Identity Object
        SearchServiceIdentityObject idO = mDatamodelFactory.createSearchServiceIdentityObject();
        
        // Set the object attributes
        idO.setIdentifier(mDatamodelFactory.createIdentifier());
        idO.setName("SearchServiceSQLLite");
        idO.setDescription("This Search Service can be used to search in the local SQLite DB.");
        
        // Returns the created Identity Object.
        return idO;
    }

    @Override
    public String describe() {
        return "the URL search service";
    }

}
