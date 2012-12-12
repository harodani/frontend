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

/**
 * Represents a locator used in NetInf Publishes.
 * @author Linus Sunde
 *
 */
public class Locator {

    /**
     * Represents a specific locator type.
     * @author Linus Sunde
     *
     */
    public enum Type {

        /** Bluetooth Locator. */
        BLUETOOTH ("btmac");

        /** The HTTP query key belonging to a specific locator type. */
        private String mKey;

        /** Creates a new locator type.
         *  @param key
         *      The HTTP query key associated with this locator type
         */
        private Type(String key) {
            mKey = key;
        }

        /**
         * Gets the HTTP query key associated with this locator type.
         * @return
         *      The HTTP query key
         */
        public String getKey() {
            return mKey;
        }

    }

    /** Locator type. */
    private Type mType;
    /** Locator. */
    private String mLocator;

    /**
     * Creates a new locator.
     * @param type
     *      The type of the locator
     * @param locator
     *      The locator, for example the Bluetooth MAC address
     */
    public Locator(Type type, String locator) {
        mType = type;
        mLocator = locator;
    }

    /**
     * Gets the HTTP query key associated with this locator.
     * @return
     *      The HTTP query key
     */
    public String getQueryKey() {
        return mType.getKey();
    }

    /**
     * Gets the HTTP query value associated with this locator.
     * @return
     *      The HTTP query value
     */
    public String getQueryValue() {
        return mLocator;
    }

}
