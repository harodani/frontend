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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;

import project.cs.lisa.util.UProperties;
import project.cs.lisa.util.metadata.Metadata;
import android.util.Log;

/**
 * Used to send NetInf Publish requests to the OpenNetInf RESTful API.
 * @author Linus Sunde
 */
public class NetInfPublish extends NetInfRequest {

    /** Log Tag. */
    public static final String TAG = "NetInfPublish";

    /** Encoding. */
    public static final String ENCODING = 
    		UProperties.INSTANCE.getPropertyWithName("httprequest.encode");

    /** Locators. */
    private Set<Locator> mLocators;

    /** File. */
    private File mFile;

    /**
     * Creates a new asynchronous NetInf PUBLISH.
     * @param host
     *      Target host for the publish
     * @param port
     *      Target port
     * @param hashAlg
     *      Hash algorithm
     * @param hash
     *      Hash
     * @param locators
     *      Set of locators to publish
     */
    public NetInfPublish(String hashAlg, String hash, Set<Locator> locators) {

        super("publish", hashAlg, hash);
        Log.d(TAG, "NetInfPublish()");
        mLocators = locators;
    }

    /**
     * Sends the NetInf PUBLISH request to the local node using HTTP.
     * @param voids
     *      Nothing.
     * @return
     *      A NetInfPublishResponse containing the status of the publish
     */
    @Override
    protected NetInfResponse doInBackground(Void... voids) {

        // Don't publish without locators
        if (mLocators == null || mLocators.size() == 0) {
            return new NetInfPublishResponse();
        }

        // Add locators
        for (Locator locator : mLocators) {
            addQuery(locator.getQueryKey(), locator.getQueryValue());
        }

        try {
            // FullPut uses HTTP POST, non-FullPut uses HTTP PUT
            HttpUriRequest request;
            if (mFile != null) {
                request = new HttpPost(getUri());
            } else {
                request = new HttpPut(getUri());
            }
            // Execute HTTP request
            HttpResponse httpResponse = execute(request);
            return new NetInfPublishResponse(httpResponse);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "Execution of HTTP publish request to local node failed");
            return new NetInfPublishResponse();
        }
    }


    /**
     * Sets the metadata to be sent in the NetInf PUBLISH message.
     * @param metadata
     *      The Metadata object containing the metadata
     */
    public void setMetadata(Metadata metadata) {
        String meta = metadata.convertToMetadataString();
        addQuery("meta", meta);
    }
    
    /**
     * Sets the content type to be sent in the NetInf PUBLISH message.
     * @param contentType
     *      The content type.
     */
    public void setContentType(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("setContentType called with null");
        }
        addQuery("ct", contentType);
    }

    /**
     * Sets the file to publish.
     * @param file
     *      The file to publish.
     */
    public void setFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("setFile() called with null file");
        }
        mFile = file;
        addQuery("filepath", file.getAbsolutePath());
    }

}
