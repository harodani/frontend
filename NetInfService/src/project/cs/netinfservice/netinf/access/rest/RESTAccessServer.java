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
package project.cs.netinfservice.netinf.access.rest;

import netinf.common.datamodel.DatamodelFactory;
import netinf.node.access.AccessServer;
import netinf.node.api.impl.LocalNodeConnection;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;


import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Provides and API to the local NetInf node.
 * 
 * @author Linus Sunde
 *
 */
public class RESTAccessServer implements AccessServer {
    /** The component handling the RESTApplication. **/
    private Component mComponent;

    /**
     * Constructor that creates a new RESTful server.
     * 
     * @param port
     *      The connection port (is injected)
     * @param connection
     *      The connection to the node 
     * @param factory
     *      Creates different objects necessary in the NetInf model
     */
    @Inject
    public RESTAccessServer(@Named("access.http.port") int port,
            LocalNodeConnection connection, DatamodelFactory factory) {
        // Component
        mComponent = new Component();
        
        // Get the component servers
        mComponent.getServers().add(Protocol.HTTP, port);
        
        // Get the application
        Application application = new RESTApplication(connection, factory);
        
        // Get Default Host
        mComponent.getDefaultHost().attach(application);
    }

    /**
     * Starts the RESTAccessServer.
     */
    public void start() {
        // Start component
        try {
            mComponent.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the RESTAccessServer.
     */  
    public void stop() {
        // Stop the server
        try {
            mComponent.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
