package project.cs.lisa.application.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import android.util.Log;

public class NetInfPublishResponse extends NetInfResponse {

    private static final String TAG = "NetInfPublishResponse";

    public NetInfPublishResponse() {
        super();
    }

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
