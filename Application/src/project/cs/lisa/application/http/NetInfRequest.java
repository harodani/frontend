package project.cs.lisa.application.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import project.cs.netinfutilities.UProperties;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Used to send requests to the OpenNetInf RESTful API.
 *
 * @author Linus Sunde
 * @author Harold Martinez
 * @author Thiago Costa Porto
 *
 */
public abstract class NetInfRequest extends AsyncTask<Void, Void, NetInfResponse> {

    /** Debug Log Tag. */
    private static final String TAG = "NetInfRequest";

    /** HTTP Scheme. */
    private static final String HTTP = 
    		UProperties.INSTANCE.getPropertyWithName("http");

    // TODO handle timeout
    /** HTTP Timeout. */
    private static final int TIMEOUT = 
    		Integer.parseInt(UProperties.INSTANCE.getPropertyWithName("httprequest.timeout"));

    /** Encoding. */
    public static final String ENCODING = 
            UProperties.INSTANCE.getPropertyWithName("httprequest.encode");

    /** Target Host. */
    private String mHost;

    /** Target Port. */
    private String mPort;

    /** Path Prefix. */
    private String mPathPrefix;

    /** The rest of the URI. **/
    private HashMap<String, String> mQueryVariables = new HashMap<String, String>();

    /** HTTP Client. **/
    private HttpClient mClient;

    /**
     * Create a new asynchronous request to send using HTTP.
     * @param host
     *      The host
     * @param port
     *      The port
     * @param pathPrefix
     *      The start of the path
     */
    protected NetInfRequest(String pathPrefix) {

        Log.d(TAG, "NetInfRequest()");

        mHost = UProperties.INSTANCE.getPropertyWithName("access.http.host");
        mPort = UProperties.INSTANCE.getPropertyWithName("access.http.port");;
        mPathPrefix = pathPrefix;

        // HTTP client with a timeout
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);
        mClient = new DefaultHttpClient(httpParams);
    }

    /**
     * Create a new asynchronous request to send using HTTP.
     * @param host
     *      Target host of the message
     * @param port
     *      Target port
     * @param hashAlg
     *      Hash algorithm used
     * @param hash
     *      Hash
     */
    public NetInfRequest(String pathPrefix,
            String hashAlg, String hash) {
        this(pathPrefix);

        addQuery("hashAlg", hashAlg);
        addQuery("hash", hash);
    }

    /**
     * Sends the request to the local node using HTTP.
     * @param voids
     *      Nothing.
     * @return
     *      The response to the request
     */
    @Override
    protected abstract NetInfResponse doInBackground(Void... voids);

    /**
     * Called on the UI thread after the response has been received.
     * @param response
     *      The response
     */
    @Override
    protected void onPostExecute(NetInfResponse response) { }

    /**
     * Executes a HTTP request to the local nodes Restlet API.
     * @param request
     *      The HTTP request
     * @return
     *      The response to the HTTP request
     * @throws IOException
     *      In case the HTTP request failed
     */
    protected HttpResponse execute(HttpUriRequest request) throws IOException {
        Log.d(TAG, "uri = " + request.getURI());

        // Execute the HTTP request
        return mClient.execute(request);
    }

    /**
     * Adds a key-value pair to the query part of the HTTP URI.
     * @param key
     *      The query key
     * @param value
     *      The value of the query key
     */
    protected void addQuery(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("addQuery called with null key");
        }
        if (value == null) {
            throw new IllegalArgumentException("addQuery called with null value");
        }
        mQueryVariables.put(key, value);
    }

    /**
     * Gets the query string representation of added query key-value pairs.
     * @return
     *      The query string
     * @throws UnsupportedEncodingException
     *      In case UTF-8 is not supported
     */
    protected String getQueryString() throws UnsupportedEncodingException {
        StringBuilder queryString = new StringBuilder();
        boolean first = true;
        for (String key : mQueryVariables.keySet()) {
            if (first) {
                queryString.append("?");
                first = false;
            } else {
                queryString.append("&");
            }
            queryString.append(URLEncoder.encode(key, "UTF-8"));
            queryString.append("=");
            queryString.append(URLEncoder.encode(mQueryVariables.get(key), "UTF-8"));
        }
        return queryString.toString();
    }

    /**
     * Creates the HTTP URI to use in the HTTP request in doInBackground.
     * @return The HTTP URI
     * @throws UnsupportedEncodingException
     *      In case UTF-8 is not supported
     */
    protected String getUri() throws UnsupportedEncodingException {
        StringBuilder uri = new StringBuilder();
        uri.append(HTTP);
        uri.append(mHost);
        uri.append(":");
        uri.append(mPort);
        uri.append("/");
        uri.append(mPathPrefix);
        uri.append(getQueryString());
        return uri.toString();
    }

}
