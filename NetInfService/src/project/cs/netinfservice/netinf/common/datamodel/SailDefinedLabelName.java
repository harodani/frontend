/* Modified by Linus and Harold
 * Based on netinf.common.datamodel.DefinedLabelName
 * added CONTENT_TYPE ("CONTENT_TYPE",6);
 *     a label that contains the content type
 * added constructors to utilise this label
 */

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

/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package project.cs.netinfservice.netinf.common.datamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * This enumeration type contains all the defined label names. The string of each defined label
 * name is the user-readable labelname within identifiers. The number of each defined label name
 * defines the ordering among the label names. This guarantees uniqueness among the labelnames.
 *
 * @author PG Augnet 2, University of Paderborn
 */
public enum SailDefinedLabelName {
    /** Authority Label */
    AUTHORITY ("AUTHORITY", 1),

    /** Hash algorithm */
    HASH_ALG ("HASH_ALG", 2),

    /** Object's Hash */
    HASH_CONTENT ("HASH_CONTENT", 3),
    
    /** File locator */
    FILE_LOCATOR ("FILE_LOCATOR", 4),
    
    /** Time to live */
    TTL ("TTL", 5),
    
    /** Mime Content Type */
    CONTENT_TYPE ("CONTENT_TYPE", 6),
    
    /** Metadata */
    META_DATA ("META_DATA", 7);

    /** Name of the label */
    private final String labelName;
    
    /** Ordering */
    private final int order;

    /**
     * Defines a label name and its placement
     * 
     * @param labelName
     *      The label name
     * @param order
     *      The placement
     */
    private SailDefinedLabelName(String labelName, int order) {
        this.labelName = labelName;
        this.order = order;
    }

    /** Gets the Sail Defined Label Name */
    public String getLabelName() {
        return this.labelName;
    }

    /** Get the order */
    public int getOrder() {
        return this.order;
    }

    /**
     * Get a SAIL defined Label Name by a string.
     * 
     * @param labelName
     *      The name of the label.
     * @return
     *      <i>SAIL defined label name</i> if successful,<br>
     *      <i>null</i> if it fails.
     */
    public static SailDefinedLabelName getDefinedLabelNameByString(String labelName) {
        SailDefinedLabelName result = null;

        // Iterate through SAIL labels
        for (SailDefinedLabelName definedLabelName : SailDefinedLabelName.values()) {
            if (definedLabelName.getLabelName().equals(labelName)) {
                // found it!
                result = definedLabelName;
                break;
            }
        }

        // Returns label
        return result;
    }

    /**
     * Returns a list with the SAIL labels.
     * 
     * @return
     *      A ArrayList with the SAIL labels.
     */
    public static List<String> valueStrings() {
        ArrayList<String> list = new ArrayList<String>();
        for (SailDefinedLabelName defLabel : values()) {
            list.add(defLabel.getLabelName());
        }
        return list;
    }

    /**
     * Checks if <i>string</i> is a SAIL label.
     * 
     * @param string
     *      The label to be checked.
     * @return
     *      <b>true</b> if string is a SAIL label, <br>
     *      <b>false</b> otherwise.
     */
    public static boolean isDefined(String string) {
        return valueStrings().contains(string);
    }
}
