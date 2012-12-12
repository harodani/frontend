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
package project.cs.netinfutilities.metadata;

import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


/**
 * Implementation of Metadata support class.
 * @author Thiago Costa Porto
 */
public class Metadata {

    /** The Logger. */
    private final static Logger LOGGER = Logger.getLogger(Metadata.class.getName()); 

    /* Metadata JSON object */
    private JSONObject mJSONObject;

    /**
     * Empty Constructor
     */
    public Metadata() {
        mJSONObject = new JSONObject();
    }

    /**
     * Constructor that takes in a already formatted JSON String
     * 
     * @param _JSONString Formatted JSON String
     */
    public Metadata(String _JSONString) {
        mJSONObject = new JSONObject();
        mJSONObject = (JSONObject) JSONValue.parse(_JSONString);

        // TODO: Maybe raise a 'malformed json string exception'
        if (mJSONObject == null) {
        	LOGGER.info("Invalid JSON String received. new object was created, but its NULL.");
        }
    }

    /**
     * Constructor for (key, value)
     * 
     * @param key String with the key
     * @param value String with the value
     */
    public Metadata(String key, String value) {
        mJSONObject = new JSONObject();
        insert(key, value);
    }

    /**
     * Constructor for string arrays. It is the developer responsibility to
     * pass arrays with the correct sizes. They are corresponding, meaning
     * key[0] goes with value[0].
     * Read it as: metadata[key(i)] = value(i)
     * 
     * @param key   Array of keys
     * @param value Array of values
     */
    public Metadata(String[] key, String[] value) {
        mJSONObject = new JSONObject();

        if (key.length != value.length) {
            LOGGER.warning("The JSON Object was created, but you gave me two arrays of "
                    + "different sizes!");
            
            if (key.length > value.length) {
                LOGGER.warning("The JSON Object created has null values.");
            }
            else {
                LOGGER.warning("The JSON Object created has lost values.");
            }
        }

        for (int i = 0; i < key.length; i++) {
            insert(key[i], value[i]);
        }
    }

    /**
     * Inserts a (key,value) to the JSON Object
     * 
     * @param key    String with key
     * @param value  Object with value
     * @return       true  if value was inserted
     *               false if value was not inserted
     */
    @SuppressWarnings("unchecked")
    public boolean insert(String key, Object value) {
        if (key == null) {
            LOGGER.warning("Tried to use a null key on insert()");
            return false;
        }

        // actual insert
        mJSONObject.put(key, value);

        return true;
    }

    /**
     * Get a value corresponding to the key
     * @param key String with the key
     * @return Value if it exists
     *         null  if things go wrong
     */
    public String get(String key) {
        if (key == null) {
            LOGGER.warning("Tried to use a null key on get()");
            return null;
        }

        if (mJSONObject == null)
            return null;

        return mJSONObject.get(key).toString();
    }

    // TODO: Beautify printing of JSON String.
    /**
     * Converts JSON Object to a FORMATTED string
     * @return Formatted string
     */
    public String convertToString() {
        return mJSONObject.toString();
    }

    /**
     * Creates a JSON string with the key "meta"
     * set to the JSON string representation of the metadata.
     * 
     * @return The JSON string
     */
    @SuppressWarnings("unchecked")
    public String convertToMetadataString() {
        JSONObject meta = new JSONObject();
        // TODO: check if this still works under json.simple
        meta.put("meta", mJSONObject);

        return meta.toString();
    }

    /**
     * Cleans the JSONObject
     */
    public void clear() {
        mJSONObject.clear();
    }
    
    public JSONObject getJSONObject() {
        return mJSONObject;
    }
}
