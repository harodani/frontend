package project.cs.lisa.application.http;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import project.cs.lisa.util.UProperties;
import android.util.Log;

public class NetInfRetrieveResponse extends NetInfResponse {

    private static final String TAG = "NetInfRetrieveResponse";

    private static String mFilePathKey =
            UProperties.INSTANCE.getPropertyWithName("restlet.retrieve.file_path");
    private static String mContentTypeKey =
            UProperties.INSTANCE.getPropertyWithName("restlet.retrieve.content_type");

    private File mFile;
    private String mContentType;

    public NetInfRetrieveResponse() {
        super();
    }

    public NetInfRetrieveResponse(HttpResponse response) {

        // TODO Remove duplicate code from NetInfResponse subclasses

        int statusCode = response.getStatusLine().getStatusCode();

        Log.d(TAG, "new NetInfRetrieveResponse, statusCode = " + statusCode);

        // Request did not succeed
        if (statusCode != HttpStatus.SC_OK) {
            setStatus(NetInfStatus.FAILED);
            return;
        }

        // No entity in response
        if (response.getEntity() == null) {
            setStatus(NetInfStatus.NO_CONTENT);
            return;
        }

        // Validate that actual JSON is returned
        String jsonString;
        try {
            jsonString = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            setStatus(NetInfStatus.INVALID_CONTENT);
            return;
        }
        Object obj = JSONValue.parse(jsonString);
        if (!(obj instanceof JSONObject)) {
            setStatus(NetInfStatus.INVALID_CONTENT);
            return;
        }
        JSONObject json = (JSONObject) obj;

        // Check for file path
        if (!json.containsKey(mFilePathKey)) {
            setStatus(NetInfStatus.NO_FILE_PATH);
            return;
        }
        mFile = new File((String) json.get(mFilePathKey));
        if (!mFile.exists()) {
            setStatus(NetInfStatus.FILE_DOES_NOT_EXIST);
            return;
        }

        // Check for content type
        if (!json.containsKey(mContentTypeKey)) {
            setStatus(NetInfStatus.NO_CONTENT_TYPE);
            return;
        }
        mContentType = (String) json.get(mContentTypeKey);

        // Everything hopefully OK
        setStatus(NetInfStatus.OK);
    }

    public File getFile() {
        return mFile;
    }

    public String getContentType() {
        return mContentType;
    }
}
