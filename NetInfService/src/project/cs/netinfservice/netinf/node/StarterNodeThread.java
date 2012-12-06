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
package project.cs.netinfservice.netinf.node;

import netinf.node.access.AccessServer;
import netinf.node.api.NetInfNode;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.ResolutionService;
import netinf.node.search.SearchController;
import netinf.node.search.SearchService;
import project.cs.netinfservice.application.MainNetInfApplication;
import android.util.Log;

public class StarterNodeThread extends Thread {

    public static final String TAG = "StarterNodeThread";

    private NetInfNode mNode;

    @Override
    public void run() {
        mNode = MainNetInfApplication.getInjector().getInstance(NetInfNode.class);

        startResolution();	// Start resolution services
        startSearch();
        startAPIAccess();	// Start REST API service
    }

    /**
     * Begin all the resolution services
     */
    private void startResolution() {
        Log.d(TAG, "startResolution()");
        Log.d(TAG, "getting resolution controller...");
        ResolutionController resolutionController = mNode.getResolutionController();

        if (resolutionController != null) {
            // Plug in Resolution Services
            Log.d(TAG, "getting resolution services...");
            ResolutionService[] resolutionServices = MainNetInfApplication.getInjector().getInstance(ResolutionService[].class);

            if (resolutionServices.length == 0) {
                Log.d(TAG, "(NODE ) I have no active resolution services");
            }

            Log.d(TAG, "adding resolution services...");
            for (ResolutionService resolutionService : resolutionServices) {
                resolutionController.addResolutionService(resolutionService);
                Log.d(TAG, "Added resolution service '" + resolutionService.getClass().getCanonicalName() + "'");
                Log.d(TAG, "(NODE ) I can resolve via " + resolutionService.describe());
            }
        }
    }

    private void startSearch() {

        SearchController searchController = mNode.getSearchController();

        if (searchController != null) {
            // Plug in Search Services
            SearchService[] searchServices = MainNetInfApplication.getInjector().getInstance(SearchService[].class);

            if (searchServices.length == 0) {
                Log.d(TAG, "(NODE ) I have no active search services");
            }

            for (SearchService searchService : searchServices) {
                searchController.addSearchService(searchService);
                Log.d(TAG, "Added search service '" + searchService.getClass().getCanonicalName() + "'");
                Log.d(TAG, "(NODE ) I can search via " + searchService.describe());
            }
        }
    }

    /**
     * Enable access to the RESTful services
     */
    private void startAPIAccess() {
        Log.d(TAG, "startAPIAccess()");
        AccessServer accessServer = MainNetInfApplication.getInjector().getInstance(AccessServer.class);
        accessServer.start();
    }
}