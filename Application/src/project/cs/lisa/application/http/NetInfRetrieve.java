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
 * Used to send NetInf GET requests to the OpenNetInf RESTful API.
 * @author Linus Sunde
 */
public class NetInfRetrieve extends NetInfRequest {

	/** Log Tag. **/
	public static final String TAG = "NetInfRetrieve";

	/**
	 * Creates a new asynchronous NetInf GET.
	 * @param host
	 *     Target host for the retrieve
	 * @param port
	 *     Target port
	 * @param hashAlg
	 *     Hash algorithm used
	 * @param hash
	 *     Hash
	 */
	public NetInfRetrieve(String host, String port,
			String hashAlg, String hash) {

		super("retrieve", hashAlg, hash);
	}

    /**
     * Sends the NetInf GET request to the local node using HTTP.
     * @paramn voids
     *      Nothing
     * @return
     *      A NetInfRetrieveResponse containing the status of the retrieve
     */
    @Override
    protected NetInfResponse doInBackground(Void... voids) {

        try {
            HttpGet get = new HttpGet(getUri());
            HttpResponse httpResponse = execute(get);
            return new NetInfRetrieveResponse(httpResponse);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "Execution of HTTP retrieve request to local node failed");
            return new NetInfRetrieveResponse();
        }

    }

}
