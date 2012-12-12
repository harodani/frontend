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
package project.cs.netinfservice.netinf.access.rest.resources;

import java.util.List;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.search.DefinedQueryTemplates;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.restlet.resource.Get;

import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.netinfservice.util.UProperties;
import project.cs.netinfservice.util.metadata.Metadata;
import android.util.Log;

/**
 * Provides Search, which is to be linked with the LisaServerResource class.
 * 
 * @author Thiago Costa Porto
 *
 */
public class SearchResource extends LisaServerResource {
    /** Debug tag */
    private static final String TAG = "SearchResource";

    /** Timeout */
    private static final int TIMEOUT = Integer.parseInt(UProperties.INSTANCE
            .getPropertyWithName("search.timeout"));

    /** Search tokens */
    private String mTokens;

    /** Implementation of DatamodelFactory, used to create and edit InformationObjects etc. **/
    private DatamodelFactory mDatamodelFactory;

    /** Node Connection, used to access the local NetInf node. **/
    private NetInfNodeConnection mNodeConnection;

    /**
     * Initiates the Search Resource.
     */
    @Override
    protected void doInit() {
        super.doInit();

        /** Get the tokens */
        mTokens = getQuery().getFirstValue("tokens", true);

        // Get data model and node
        mDatamodelFactory = getDatamodelFactory();
        mNodeConnection   = getNodeConnection();
    }

    /**
     * Search handler.
     * 
     * @return
     *      <i>JSON String</i> from the JSON Object that contains the NetInf response,<br>
     *      <i>null</i> if something failed.  
     * @throws NetInfCheckedException
     * 
     */
    @SuppressWarnings("unchecked") // JSON Array
    @Get
    public String search() throws NetInfCheckedException {
        Log.d(TAG, "RESTful API received search request");
        try {
            // Perform search and hold the results into a list of identifiers
            List<Identifier> results = mNodeConnection.performSearch(DefinedQueryTemplates.URL,
                    new String[] { mTokens }, TIMEOUT);

            // TODO: Lets return a proper JSONObject.toString instead of a hardcoded string!
            if (results.isEmpty()) {
                return "{\"results\":[]}";
            }
            
            // Only use the first identifier (we are insterested in the first response)
            Identifier identifier = results.get(0);

            // Create a JSON Object from the results
            JSONObject jsonObject = new JSONObject();
            JSONArray resultArray = new JSONArray();
            resultArray.add(identifierToJson(identifier));
            jsonObject.put("results", resultArray);

            // Return the JSON String
            return jsonObject.toString();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a JSON Object from an Identifier.
     * 
     * @param identifier
     *      The identifier from the response.
     * @return
     *      A <i>JSON Object</i> created from the Identifier.
     */
    @SuppressWarnings("unchecked") // JSON Object
    private JSONObject identifierToJson(Identifier identifier) {
        // Attributes
        String hash = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_CONTENT
                .getLabelName()).getLabelValue();
        String hashAlg = identifier.getIdentifierLabel(SailDefinedLabelName.HASH_ALG
                .getLabelName()).getLabelValue();
        String meta = identifier.getIdentifierLabel(SailDefinedLabelName.META_DATA
                .getLabelName()).getLabelValue();

        // TODO: Skip unnecessary calls here. Either do it as Metadata or JSON Object.
        // Construct the JSON Object
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ni", "ni://" + hashAlg + ";" + hash);
        jsonObject.put("status", 200);

        // Create a Metadata from the created JSON
        Metadata metadata = new Metadata(meta);
        
        // Revert it back
        jsonObject.put("meta", metadata.getJSONObject());

        // Return the JSON Object
        return jsonObject;
    }
}
