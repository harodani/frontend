package project.cs.lisa.application.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class NetInfPublishResponse extends NetInfResponse {

    public NetInfPublishResponse() {
        super();
    }

    public NetInfPublishResponse(HttpResponse response) {

        // TODO Remove duplicate code from NetInfResponse subclasses

        int statusCode = response.getStatusLine().getStatusCode();

        // Request did not succeed
        if (statusCode != HttpStatus.SC_OK) {
            setStatus(NetInfStatus.FAILED);
            return;
        }

        // No entity in response
        if (response.getEntity() == null) {
            setStatus(NetInfStatus.NO_JSON);
            return;
        }

        // Validate that actual JSON is returned
        String jsonString;
        try {
            jsonString = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            setStatus(NetInfStatus.NO_JSON);
            return;
        }
        Object obj = JSONValue.parse(jsonString);
        if (!(obj instanceof JSONObject)) {
            setStatus(NetInfStatus.INVALID_JSON);
            return;
        }

        // Everything hopefully OK
        setStatus(NetInfStatus.OK);
    }
}
