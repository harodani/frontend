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
    public NetInfSearch(String tokens, String ext) {
        super("search");

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

        try {
            HttpGet search = new HttpGet(getUri());
            HttpResponse httpResponse = execute(search);
            return new NetInfSearchResponse(httpResponse);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "Execution of HTTP search request to local node failed");
            return new NetInfSearchResponse();
        }
    }
}
