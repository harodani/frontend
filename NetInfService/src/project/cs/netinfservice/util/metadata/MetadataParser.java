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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.util.Log;

/**
 * Class for parsing json metadata.
 * @author Thiago Costa Porto
 */
public class MetadataParser {
    public static final String TAG = "MetadataParser";

    public static final String TAG_NetInf = "NetInf";
    public static final String TAG_msgId = "msgId";
    public static final String TAG_status = "status";
    public static final String TAG_ni = "ni";
    public static final String TAG_timestamp = "ts";
    public static final String TAG_metadata = "metadata";
    public static final String TAG_loc = "loc";
    public static final String TAG_meta = "meta";

    // JSON Object
    private JSONObject mJSONMetadata;

    /**
     * Returns a map that represents the meta-data key value pairs
     * contained in the specified meta-data.
     *
     * @param metadata          The JSON object corresponding to the meta-data.
     * @return                  The map with all meta-data values
     * @throws JSONException    Thrown, if no meta-data could be extracted at all
     */
    public static Map<String, Object> toMap(JSONObject metadata) throws JSONException {
        // TODO: Warning: for compability issues, I am still throwing JSONException here. We must
        //       fix the Database class to stop catching JSONException. If it is of good practice,
        //       we can create an exception and throw it here.
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        metadata = (JSONObject) metadata.get("meta");

        if (metadata == null) {
            throw new JSONException("\"meta\" tag not present in JSON Object");
        }

        // We iterate through the metadata by getting a set of keys from the metadata and
        // we walk through that set.
        @SuppressWarnings("unchecked")
        Set<String> keys = (Set<String>) metadata.keySet();

        Iterator<String> iterator = keys.iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value;

            value = metadata.get(key);

            if (value instanceof JSONArray) {
                List<String> list = extractList((JSONArray) value);
                map.put(key, list);
            } else {
                map.put(key, value);
            }
        }

        if (map.size() == 0) {
            throw new JSONException("No meta-data could be extracted.");
        }

        return map;
    }

    /*
    public static Map<String, Object> toMap(org.json.JSONObject metadata) throws JSONException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        try {
            metadata = (org.json.JSONObject) metadata.get("meta");
        } catch (JSONException e) {
            Log.e(TAG, "Meta-data couldn't be extracted.");
            throw new JSONException("Extracting the metadata with the meta tag failed.");
        }

        //metada.keys does not have a defined type but it always will be a String
        @SuppressWarnings("unchecked")
        Iterator<String> iterator = metadata.keys();

        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value;

            try {
                value = metadata.get(key);

                if (value instanceof JSONArray) {
                    List<String> list = extractList((JSONArray) value);
                    System.out.println("list metadata parser:" + list.toString());
                    map.put(key, list);
                } else {
                    map.put(key, value);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Extracting a value in a meta-data field failed");
                e.printStackTrace();
            }
        }

        if (map.size() == 0) {
            throw new JSONException("No meta-data could be extracted.");
        }

        return map;
    }*/

    /**
     * Converts a json array into a collection of corresponding string values.
     *
     * @param jsonArray         The specified json array to convert.
     * @return                  A collection containing the corresponding String values.
     * @throws JSONException    Throws a JSONException in case an array element could not
     *                          be retrieved.
     */
    private static List<String> extractList(JSONArray jsonArray) {
        List<String> list = new ArrayList<String>();

        // length of JSON Array
        int len = jsonArray.size();

        // Gets the JSON Array item using the index
        for (int i = 0; i < len; i++) {
            list.add(jsonArray.get(i).toString());
        }

        return list;
    }
}
