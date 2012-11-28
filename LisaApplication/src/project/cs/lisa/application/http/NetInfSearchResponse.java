package project.cs.lisa.application.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import project.cs.lisa.util.UProperties;

/**
 * Represents a response to a NetInfSearch.
 * @author Linus Sunde
 */
public class NetInfSearchResponse extends NetInfResponse {

    /** Search results JSON Key used by the RESTful API. */
    private static final String RESULTS_KEY =
            UProperties.INSTANCE.getPropertyWithName("restlet.search.results");

    /** Search Results. */
    private JSONArray mSearchResults;

    /**
     * Creates a new response for a unsent search.
     */
    public NetInfSearchResponse() {
        super();
    }

    /**
     * Creates a new response given the HTTP response to a sent search.
     * @param response
     *      The HTTP response
     */
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
            setStatus(NetInfStatus.NO_CONTENT);
            return;
        }

        // Validate that actual JSON is returned
        String jsonString;
        try {
            jsonString = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            setStatus(NetInfStatus.NO_CONTENT);
            return;
        }
        Object obj = JSONValue.parse(jsonString);
        if (!(obj instanceof JSONObject)) {
            setStatus(NetInfStatus.INVALID_CONTENT);
            return;
        }
        JSONObject json = (JSONObject) obj;

        // Check for search results
        if (!json.containsKey(RESULTS_KEY)) {
            setStatus(NetInfStatus.NO_SEARCH_RESULTS);
            return;
        }
        Object resultsObj = json.get(RESULTS_KEY);
        if (!(resultsObj instanceof JSONArray)) {
            setStatus(NetInfStatus.INVALID_SEARCH_RESULTS);
            return;
        }
        mSearchResults = (JSONArray) resultsObj;

        // Everything hopefully OK
        setStatus(NetInfStatus.OK);
    }

    /**
     * Gets the content type of the retrieved file.
     * @return
     *      The content type of the retrieved file
     * @throws RequestFailedException
     *      In case the method is called on a failed request
     */
    public JSONArray getSearchResults() throws RequestFailedException {
        if (getStatus() != NetInfStatus.OK) {
            throw new RequestFailedException("getSearchResults() called on failed search");
        }
        return mSearchResults;
    }
}
