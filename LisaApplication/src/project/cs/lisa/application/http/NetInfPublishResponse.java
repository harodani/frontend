package project.cs.lisa.application.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import android.util.Log;

/**
 * Represents a response to a NetInfPublish.
 * @author Linus Sunde
 */
public class NetInfPublishResponse extends NetInfResponse {

    /** Log Tag. */
    private static final String TAG = "NetInfPublishResponse";

    /**
     * Creates a new response for a unsent publish.
     */
    public NetInfPublishResponse() {
        super();
    }

    /**
     * Creates a new response given the HTTP response to a sent publish.
     * @param response
     *      The HTTP response
     */
    public NetInfPublishResponse(HttpResponse response) {

        // TODO Remove duplicate code from NetInfResponse subclasses

        int statusCode = response.getStatusLine().getStatusCode();

        Log.d(TAG, "new NetInfPublishResponse, statusCode = " + statusCode);

        // Request did not succeed
        if (statusCode != HttpStatus.SC_OK) {
            setStatus(NetInfStatus.FAILED);
            return;
        }

        // Everything hopefully OK
        setStatus(NetInfStatus.OK);
    }
}
