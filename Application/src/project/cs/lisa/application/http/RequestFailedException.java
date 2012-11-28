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
 * Thrown when trying trying to get information from response to a failed request.
 * @author Linus Sunde
 */
public class RequestFailedException extends Exception {
    /**
     * Constructs a NullEntityException.
     */
    public RequestFailedException() {
        super();
    }

    /**
     * Constructs a ResponseFailedException with the specified detail message.
     * @param message
     *      the detail message.
     */
    public RequestFailedException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResponseFailedException with the specified detail message and cause.
     * @param message
     *      the detail message
     * @param cause
     *      the cause
     */
    public RequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

