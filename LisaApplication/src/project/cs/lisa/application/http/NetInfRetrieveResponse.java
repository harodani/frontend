package project.cs.lisa.application.http;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import project.cs.lisa.util.UProperties;

public class NetInfRetrieveResponse extends NetInfResponse {

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
        JSONObject json = (JSONObject) obj;

        // Check for file path
        if (!json.containsKey(mFilePathKey)) {
            setStatus(NetInfStatus.NO_FILE_PATH);
            return;
        }
        mFile = new File((String) json.get(mFilePathKey));
        if (!mFile.exists()) {
            setStatus(NetInfStatus.NO_FILE);
            return;
        }

        // Check for content type
        if (!json.containsKey(mContentTypeKey)) {
            setStatus(NetInfStatus.INVALID_JSON);
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
