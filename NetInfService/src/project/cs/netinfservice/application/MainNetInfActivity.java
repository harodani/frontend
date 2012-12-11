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

import java.io.IOException;

import project.cs.netinfservice.netinf.node.StarterNodeThread;
import project.cs.netinfservice.netinf.provider.bluetooth.BluetoothDiscovery;
import project.cs.netinfservice.netinf.server.bluetooth.BluetoothServer;
import project.cs.netinfservice.util.UProperties;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


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

    /** Preference file. */
    private static final String PREF_FILE = "NetInfServicePrefsFile";

    /** Message communicating if the node were started successfully. */
    public static final String NODE_STARTED_MESSAGE = "project.cs.list.node.started";

    /** Thread for staring a NetInf node. */
    private StarterNodeThread mStarterNodeThread;

    /** Bluetooth server for serving bluetooth devices. */
    private BluetoothServer mBluetoothServer;

    /** A broadcast receiver for intercepting Bluetooth activity. */
    private BroadcastReceiver mBroadcastReceiver;

    /** The filter for choosing what actions the broadcast receiver will catch. */
    private IntentFilter mIntentFilter;

    /** Activity. */
    private static MainNetInfActivity sMainNetInfActivity;

    /** Handles the Bluetooth discovery interval. */
    private Handler mHandler;


    /**
     * Creates a Listener for the preference menu.
     */
    private OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_key_bluetooth")) {
                if (sharedPreferences.getBoolean(key, false)) {
                    startBluetoothServer();
                } else {
                    stopBluetoothServer();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        sMainNetInfActivity = this;

        PreferenceManager.getDefaultSharedPreferences(getActivity())
        .registerOnSharedPreferenceChangeListener(mListener);

        // Turn on the Bluetooth server if Bluetooth is enabled
        startBluetoothServer();

        // Setup a broadcast receiver for being notified when the Bluetooth is enabled/disabled
        setUpBroadcastReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        MainNetInfApplication.getAppContext().registerReceiver(mBroadcastReceiver, mIntentFilter);

        setupNode();

        // Display the settings fragment as the main content.
        // setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new SettingsFragment())
        .commit();

        //Initialize a handler
        mHandler = new Handler();

        
        
        runBluetoothDiscoveryBackground();
        
        /*
         * Set up some notification depending on the connection: colors.
         * Ask Paolo, he knows.
         */


        runBluetoothDiscoveryBackground();
    }

    /**
     * Creates and run a thread that is run every interval ms
     * that runs the Bluetooth Discovery in background.
     */
    private void runBluetoothDiscoveryBackground() {

    	Runnable bluetoothTask = new Runnable() {
			
			@Override
			public void run() {
				Log.d(TAG, "Discovering bluetooth. ");
				
				BluetoothDiscovery btDiscovery = BluetoothDiscovery.INSTANCE;
				btDiscovery.startBluetoothDiscovery();
				int interval = Integer.parseInt(UProperties.INSTANCE
		        		.getPropertyWithName("bluetooth.interval"));
		        mHandler.postDelayed(this, interval);
				
		        
			}
		};
		if (BluetoothAdapter.getDefaultAdapter() == null) {
			showToast(""); 
			Log.w(TAG, "No Bluetooh adapter available. Bluetooth discovery canceled.");
		}		
		else new Thread(bluetoothTask).start();
    		
		
	}



	/**
     * Stops the Bluetooth server.
     */
    private void stopBluetoothServer() {
        Log.d(TAG, "Stopping Bluetooth server");
        if (mBluetoothServer != null && mBluetoothServer.isAlive()) {
            mBluetoothServer.cancel();
        }
    }

    /**
     * Starts the Bluetooth server.
     */
    private void startBluetoothServer() {
        Log.d(TAG, "Starting Bluetooth server");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showToast("No Bluetooh adapter available.");
            /* TODO: Disable Bluetooth services:
             * Server, Provider, change Share option regarding fullput,
             * do not add device as locator.
             */
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                try {
                    mBluetoothServer = new BluetoothServer();
                    mBluetoothServer.start();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
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
     * Sets up a broadcast receiver for intercepting Bluetooth states.
     */
    private void setUpBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        startBluetoothServer();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        stopBluetoothServer();
                        break;
                    default:
                        // We don't care of any other states.
                        break;
                    }
                }
            }
        };
    }

    /**
     * Show a toast.
     * @param text      The text to show in the toast.
     */
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    /**
     * Return an instance of this activity.
     * @return the activity
     */
    public static MainNetInfActivity getActivity() {
        return sMainNetInfActivity;
    }
}