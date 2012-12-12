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
 * Represents a response to a NetInf request.
 * @author Linus Sunde
 *
 */
public abstract class NetInfResponse {

    /** Status of the request. */
    private NetInfStatus mStatus;

    /**
     * Creates a new response for a unsent request.
     */
    protected NetInfResponse() {
        mStatus = NetInfStatus.NOT_EXECUTED;
    }

    /**
     * Creates a new response with a given status.
     * @param status
     *      The status
     */
    protected void setStatus(NetInfStatus status) {
        mStatus = status;
    }

    /**
     * Gets the status of the response.
     * @return
     *      The status
     */
    public NetInfStatus getStatus() {
        return mStatus;
    }

}
