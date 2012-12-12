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
package project.cs.netinfservice.netinf.node.module;

import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.MessageEncoder;
import netinf.common.communication.MessageEncoderProtobuf;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;
import netinf.node.access.AccessServer;
import netinf.node.api.NetInfNode;
import netinf.node.api.impl.NetInfNodeImpl;
import netinf.node.api.impl.NetInfNodeReceiveHandler;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.ResolutionServiceSelector;
import netinf.node.resolution.impl.ResolutionControllerImplWithoutSecurity;
import netinf.node.resolution.impl.SimpleResolutionServiceSelector;
import netinf.node.search.SearchController;
import netinf.node.search.SearchService;
import netinf.node.search.impl.SearchControllerImpl;
import netinf.node.transfer.TransferController;
import netinf.node.transfer.impl.TransferControllerImpl;

import org.apache.commons.lang.ArrayUtils;

import project.cs.netinfservice.database.IODatabase;
import project.cs.netinfservice.database.IODatabaseFactory;
import project.cs.netinfservice.netinf.access.rest.RESTAccessServer;
import project.cs.netinfservice.netinf.node.resolution.LocalResolutionService;
import project.cs.netinfservice.netinf.node.resolution.NameResolutionService;
import project.cs.netinfservice.netinf.node.search.UrlSearchService;
import project.cs.netinfutilities.UProperties;
import android.util.Log;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;

/**
 * Bindings.
 * @author Linus Sunde
 *
 */
public class Module extends AbstractModule  {

	/** Debug tag. */
    public static final String TAG = "Module";

    /** Constructor */
    public Module() {
        Log.d(TAG, "Module()");
    }

    @Override
    protected void configure() {
        Log.d(TAG, "configure()");

        Log.d(TAG, "bindProperties()");
        Names.bindProperties(binder(), UProperties.INSTANCE.getProperty());

        Log.d(TAG, "Binding 1");
        bind(MessageEncoder.class).to(MessageEncoderProtobuf.class).in(Singleton.class);

        Log.d(TAG, "Binding 2");
        bind(DatamodelFactory.class).to(DatamodelFactoryImpl.class);

        Log.d(TAG, "Binding 3");
        bind(NetInfNode.class).to(NetInfNodeImpl.class).in(Singleton.class);

        Log.d(TAG, "Binding 4");
        bind(AsyncReceiveHandler.class).to(NetInfNodeReceiveHandler.class);

        Log.d(TAG, "Binding 5");
        bind(ResolutionController.class).to(
                ResolutionControllerImplWithoutSecurity.class).in(Singleton.class);

        Log.d(TAG, "Binding 6");
        bind(ResolutionServiceSelector.class).to(SimpleResolutionServiceSelector.class);

        Log.d(TAG, "Binding 7");
        bind(TransferController.class).to(TransferControllerImpl.class).in(Singleton.class);

        Log.d(TAG, "Binding 8");
        bind(AccessServer.class).to(RESTAccessServer.class).in(Singleton.class);

        Log.d(TAG, "Binding 9");
        bind(IODatabaseFactory.class)
        .toProvider(FactoryProvider.newFactory(IODatabaseFactory.class, IODatabase.class));

        Log.d(TAG, "Binding 10");
        bind(UrlSearchService.class);

        Log.d(TAG, "Binding 11");
        bind(SearchController.class).to(SearchControllerImpl.class).in(Singleton.class);
    }

    /**
     * This method provides all the {@link ResolutionService}s which are automatically
     * inserted into the node. In order to get an
     * instance of the according {@link ResolutionService}, add an additional parameter
     * to this method, since this puts GUICE in
     * charge of creating the correct instance of the according service.
     *
     * @param 	nrs	The name resolution service
     * @param 	lrs	The local resolution service
     * @return 	The list of resulution services
     */
    @Singleton
    @Provides
    ResolutionService[] provideResolutionServices(NameResolutionService nrs,
            LocalResolutionService lrs) {

        ResolutionService[] localResolutionService  = { lrs };
        ResolutionService[] nameResolutionService = { nrs };

        return (ResolutionService[]) ArrayUtils
        		.addAll(nameResolutionService, localResolutionService);
    }


    @Singleton
    @Provides
    SearchService[] provideSearchServices(UrlSearchService searchServiceSQLite) {
       return new SearchService[] { searchServiceSQLite };
    }

}
