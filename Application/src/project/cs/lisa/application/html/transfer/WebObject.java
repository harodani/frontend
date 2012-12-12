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

package project.cs.lisa.application.html.transfer;

import java.io.File;

/**
 * A representation of a web object.
 *
 * @author Paolo Boschini
 * @author Linus Sunde
 * @author Kim-Anh Tran
 *
 */
public class WebObject {

    /** The content type associated to this web object. */
    private String mContentType;

    /** The file associated to this web object. */
    private File mFile;
    
    /** The hash associated to this web object. */
    private String mHash;
   
    /**
     * Default constructor. Created a new web object.
     * 
     * @param contentType   the content type of this web object
     * @param file          the file that contains this web object
     * @param hash          the hash of this web object
     */
    public WebObject(String contentType, File file, String hash) {
        mContentType = contentType;
        mFile = file;
        mHash = hash;
    }

    /**
     * Returns the content type of this web object.
     * @return the content type
     */
    public String getContentType() {
        return mContentType;
    }

    /**
     * Returns the file of this web object.
     * @return the file
     */
    public File getFile() {
        return mFile;
    }

    /**
     * Returns the hash of this web object.
     * @return  the hash
     */
    public String getHash() {
        return mHash;
    }
}
