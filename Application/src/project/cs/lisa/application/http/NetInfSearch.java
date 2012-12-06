package project.cs.lisa.application.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.util.Log;

/**
 * Search functionality implementation on NetInfRequest level. From here,
 * the request should reach the REST service.
 * @author Thiago Costa Porto
 */
public class NetInfSearch extends NetInfRequest {

    /** Debug tag. **/
    public static final String TAG = "NetInfSearch";

    /**
     * Creates a new asynchronous NetInf SEARCH.
     * @param host         Target host of the message
     * @param port         Target port
     * @param tokens       Keywords to be searched
     * @param ext          Extensions
     */
    public NetInfSearch(String host, String port, String tokens, String ext) {
        super(host, port, "search");

        // Add extension and tokens fields to URI
        // TODO: When ext is filled, check if encoding is necessary
        addQuery("ext", ext);
        addQuery("tokens", tokens);
    }

    /**
     * Asks the NetInf node to search for URL using HTTP.
     * @param   voids   Nothing.
     * @return          JSON response from the NetInf node
     *                  or null if the request failed
     */
    @Override
    protected NetInfResponse doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground()");

        try {
            HttpGet search = new HttpGet(getUri());
            HttpResponse httpResponse = execute(search);
            return new NetInfSearchResponse(httpResponse);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage() != null ? e.getMessage() : "Execution of HTTP search request to local node failed");
            return new NetInfSearchResponse();
        }
    }
}
