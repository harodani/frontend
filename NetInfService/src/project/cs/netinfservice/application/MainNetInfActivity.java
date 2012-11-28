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
package project.cs.netinfservice.application;

import project.cs.netinfservice.R;
import project.cs.netinfservice.netinf.node.StarterNodeThread;
import project.cs.netinfservice.netinf.server.bluetooth.BluetoothServer;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


/**
 * Main activity that acts as a starting point for the application.
 * It provides functions setting up the NetInf services.
 *
 * @author Paolo Boschini
 * @author Linus Sunde
 *
 */
public class MainNetInfActivity extends Activity {

    /** Debugging tag. */
    private static final String TAG = "MainNetInfActivity";

    /** Represents the number of attempts to initialize a BluetoothServer. */
    private static final int NUMBER_OF_ATTEMPTS = 2;

    /** Message communicating if the node were started successfully. */
    public static final String NODE_STARTED_MESSAGE = "project.cs.list.node.started";

    /** Thread for staring a NetInf node. */
    private StarterNodeThread mStarterNodeThread;

    /** Bluetooth server for serving bluetooth devices. */
    private BluetoothServer mBluetoothServer;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_main);

        setupNode();
        /*
         * Check: Where should we start the Bluetooth Server if we don't
         * ask for the share option within this application?
         * setupBluetoothServer();
         */
        
        // Set up some feedback depending on the connection: colors 
    }
   

    /**
     * Initialize and run the StarterNodeThread.
     */
    private void setupNode() {
        Log.d(TAG, "setupNode()");
        
        mStarterNodeThread = new StarterNodeThread();
        mStarterNodeThread.start();
    }

    /**
     * Initiates and starts the Bluetooth Server.
     *
    private void setupBluetoothServer() {
        Log.d(TAG, "setupBluetoothServer()");

        // Tries to initialize the Bluetooth Server several times, if unsuccessful.
        int attempts = NUMBER_OF_ATTEMPTS;
        do {
            try {
                mBluetoothServer = new BluetoothServer();
                mBluetoothServer.start();
            } catch (IOException e) {
                --attempts;
                mBluetoothServer = null;
            }
        } while (mBluetoothServer == null && attempts > 0);

        if (mBluetoothServer == null) {
            Log.e(TAG, "BluetoothServer couldn't be initialized.");
        }
    }
    /**/
}