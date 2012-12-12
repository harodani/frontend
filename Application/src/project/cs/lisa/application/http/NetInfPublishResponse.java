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
