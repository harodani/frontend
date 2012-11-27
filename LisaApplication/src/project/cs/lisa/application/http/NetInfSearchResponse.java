package project.cs.lisa.application.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import project.cs.lisa.util.UProperties;

public class NetInfSearchResponse extends NetInfResponse {

    private static final String mResultsKey =
            UProperties.INSTANCE.getPropertyWithName("restlet.search.results");
    private static final String mTimestampKey =
            UProperties.INSTANCE.getPropertyWithName("restlet.search.timestamp");

    JSONArray mSearchResults;

    public NetInfSearchResponse() {
        super();
    }

    public NetInfSearchResponse(HttpResponse response) {

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

        // Check for search results
        if (!json.containsKey(mResultsKey)) {
            setStatus(NetInfStatus.NO_SEARCH_RESULTS);
            return;
        }
        Object resultsObj = json.get(mResultsKey);
        if (!(resultsObj instanceof JSONArray)) {
            setStatus(NetInfStatus.INVALID_SEARCH_RESULTS);
            return;
        }
        mSearchResults = (JSONArray) resultsObj;

        // Everything hopefully OK
        setStatus(NetInfStatus.OK);
    }

    public JSONArray getSearchResults() {
        return mSearchResults;
    }
}
