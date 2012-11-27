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

    /** Debug Log Tag. **/
    private static final String TAG = "NetInfRequest";

    // TODO inject from properties
    /** HTTP Scheme. **/
    private static final String HTTP = "http://";

    // TODO inject from properties
    /** HTTP Timeout. **/
    private static final int TIMEOUT = 6000000;

    // TODO inject from properties
    /** Target Host. **/
    private String mHost;

    // TODO inject from properties
    /** Target Port. **/
    private String mPort;

    /** Path Prefix. **/
    private String mPathPrefix;

    /** The rest of the URI. **/
    private HashMap<String, String> mQueryVariables = new HashMap<String, String>();

    /** HTTP Client. **/
    private HttpClient mClient;

    public NetInfRequest(String host, String port, String pathPrefix) {

        Log.d(TAG, "NetInfRequest()");

        mHost = host;
        mPort = port;
        mPathPrefix = pathPrefix;

        // HTTP client with a timeout
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);
        mClient = new DefaultHttpClient(httpParams);
    }

    /**
     * Create a new asynchronous NetInf message sent using HTTP GET.
     * @param host         Target host of the message
     * @param port         Target port
     * @param hashAlg      Hash algorithm used
     * @param hash         Hash
     */
    public NetInfRequest(String host, String port, String pathPrefix,
            String hashAlg, String hash) {
        this(host, port, pathPrefix);

        Log.d(TAG, "NetInfRequest()");

        addQuery("hashAlg", hashAlg);
        addQuery("hash", hash);
    }

    /**
     * Sends the NetInf request to the local node using HTTP.
     * @param   voids   Nothing.
     * @return          JSON response to the NetInf request sent as HTTP
     *                  or null if the request failed.
     */
    @Override
    protected abstract NetInfResponse doInBackground(Void... voids);

    /**
     * Handles the response to the request sent to the NetInf node.
     * @param jsonResponse     The JSON response.
     */
    @Override
    protected void onPostExecute(NetInfResponse response) {
        Log.d(TAG, "onPostExecute()");
    }

    protected HttpResponse execute(HttpUriRequest request) throws IOException {
        Log.d(TAG, "execute()");
        Log.d(TAG, "uri = " + request.getURI());
        Log.d(TAG, "class = " + request.getClass().toString());
        Log.d(TAG, "method = " + request.getMethod().toString());

        // Execute the HTTP request
        return mClient.execute(request);
    }

    /**
     * Adds a key-value pair to the query part of the HTTP URI.
     * @param key   The query key
     * @param value The value of the query key
     */
    protected void addQuery(String key, String value) {
        Log.d(TAG, "addQuery()");
        Log.d(TAG, "key = " + key);
        Log.d(TAG, "value = " + value);
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
        Log.d(TAG, "getQueryString()");
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
        Log.d(TAG, "getUri()");
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
