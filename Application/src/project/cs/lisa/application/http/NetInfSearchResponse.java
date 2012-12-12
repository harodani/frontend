/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */

package project.cs.lisa.application.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import project.cs.netinfutilities.UProperties;

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
        
        if (mSearchResults.isEmpty()) {
        	setStatus(NetInfStatus.NO_SEARCH_RESULTS);
        } else {
	        // Everything hopefully OK
	        setStatus(NetInfStatus.OK);
        }
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
