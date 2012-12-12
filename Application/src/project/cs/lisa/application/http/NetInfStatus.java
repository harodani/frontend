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
 * Represent the status of a response to a request.
 * @author Linus Sunde
 *
 */
public enum NetInfStatus {

    /** Request Succeeded. */
    OK,
    /** Request Failed. */
    FAILED,
    /** Response to request contained no content when it should have. */
    NO_CONTENT,
    /** Response to request contained invalid content. */
    INVALID_CONTENT,
    /** Response contained no file path when it should have. */
    NO_FILE_PATH,
    /** Response contained no content type when it should have. */
    NO_CONTENT_TYPE,
    /** Response contained a file path to a file that doesn't exist. */
    FILE_DOES_NOT_EXIST,
    /** Response contained no search results when it should have. */
    NO_SEARCH_RESULTS,
    /** Response contained invalid search results. */
    INVALID_SEARCH_RESULTS,
    /** Request was not executed. */
    NOT_EXECUTED;

}
