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
package project.cs.netinfservice.util.metadata;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.util.Log;

/**
 * Implementation of Metadata support class.
 * 
 * @author Thiago Costa Porto
 */
public class Metadata {
    /** Debug Tag */
    private final String TAG = "MetadataClass";

    /** Metadata JSON object */
    private JSONObject mJSONObject;

    /**
     * Default Constructor.
     */
    public Metadata() {
        // Initializes SIMPLEJSON JSON Object
        mJSONObject = new JSONObject();
    }

    /**
     * Constructor that takes in a already formatted JSON String
     *
     * @param jsonString
     *      Formatted JSON String
     */
    public Metadata(String jsonString) {
        Log.d(TAG, "Metadata(string) Constructor");
        Log.d(TAG, "JSON String received:\n" + jsonString);

        // Initializes new SimpleJson object
        mJSONObject = new JSONObject();
        
        // Parse JSON String into a new Object
        mJSONObject = (JSONObject) JSONValue.parse(jsonString);

        // TODO: Maybe raise a 'malformed json string exception'?
        // Check if Metadata was created.
        if (mJSONObject != null) {
            Log.d(TAG, "Metadata created:\n" + mJSONObject);
            Log.d(TAG, "Keys present in the created JSON:\n" + mJSONObject.keySet().toString());
        } else {
            Log.d(TAG, "Invalid JSON String received. new object was created, but its NULL.");
        }
    }

    /**
     * Constructor for (key, value)
     *
     * @param key
     *      String with the key
     * @param value
     *      String with the value
     */
    public Metadata(String key, String value) {
        // New SimpleJson object
        mJSONObject = new JSONObject();
        
        // Add "key" : "value" to JSON Object
        insert(key, value);
    }

    /**
     * Constructor for string arrays. It is the developer responsibility to
     * pass arrays with the correct sizes. They are corresponding, meaning
     * key[0] goes with value[0].
     * 
     * Read it as: metadata[key(i)] = value(i)
     *
     * @param key
     *      Array of keys
     * @param value
     *      Array of values
     */
    public Metadata(String[] key, String[] value) {
        // New SimpleJson object
        mJSONObject = new JSONObject();

        // Check if both arrays have the same size
        if (key.length != value.length) {
            // Different size arrays
            Log.d(TAG, "The JSON Object was created, but you gave me two arrays of "
                    + "different sizes!");

            // TODO: HANDLE this. Urgently.
            // Null or lost values
            if (key.length > value.length) 
                Log.d(TAG, "The JSON Object created has null values.");
            else 
                Log.d(TAG, "The JSON Object created has lost values.");
        }

        // Add keys
        for (int i = 0; i < key.length; i++) {
            insert(key[i], value[i]);
        }
    }

    /**
     * Inserts a (key,value) to the JSON Object.
     *
     * @param key
     *      String with key
     * @param value
     *      Object with value
     * @return
     *      <i>true</i>  if value was inserted<br>
     *      <i>false</i> if value was not inserted
     */
    @SuppressWarnings("unchecked")
    public boolean insert(String key, Object value) {
        // Sanity check!
        if (key == null) {
            Log.d(TAG, "Tried to use a null key on insert()");
            // Fails
            return false;
        }

        // actual insert
        mJSONObject.put(key, value);

        // Success!
        return true;
    }

    /**
     * Get a value corresponding to the key.
     * 
     * @param key
     *      String with the key
     * @return
     *      <i>Value<i> if it exists<br>
     *      <i>null</i>  if things go wrong
     */
    public String get(String key) {
        if (key == null) {
            Log.d(TAG, "Tried to use a null key on get()");
            return null;
        }

        // Sanity check
        if (mJSONObject == null)
            return null;

        // Returns the key (if it is there)
        return mJSONObject.get(key).toString();
    }

    // TODO: Beautify printing of JSON String.
    /**
     * Converts JSON Object to a FORMATTED string.
     * 
     * @return
     *      JSON Object as a string.
     */
    public String convertToString() {
        return mJSONObject.toString();
    }

    /**
     * Creates a JSON string with the key "meta" set to the JSON string representation of the
     * metadata.
     *
     * @return
     *      The JSON Object as a string.
     */
    @SuppressWarnings("unchecked")
    public String convertToMetadataString() {
        JSONObject meta = new JSONObject();
        meta.put("meta", mJSONObject);

        return meta.toString();
    }

    /**
     * Cleans the JSONObject
     */
    public void clear() {
        mJSONObject.clear();
    }

    /**
     * Returns the JSON Object.
     * 
     * @return
     *      JSON Object.
     */
    public JSONObject getJSONObject() {
        return mJSONObject;
    }
}
