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
package project.cs.lisa.application;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import project.cs.lisa.R;
import project.cs.lisa.application.dialogs.ListDialog;
import project.cs.lisa.application.dialogs.OkButtonDialog;
import project.cs.lisa.application.html.transfer.FetchWebPageTask;
import project.cs.lisa.application.wifi.WifiHandler;
import project.cs.lisa.networksettings.BTHandler;
import project.cs.lisa.util.UProperties;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Main activity that acts as a starting point for the application.
 * It provides functions for the user interaction and for setting up
 * the application.
 *
 * @author Paolo Boschini
 * @author Linus Sunde
 *
 */
public class MainNetInfActivity extends Activity {

    /** Debugging tag. */
    private static final String TAG = "MainNetInfActivity";
    
    /** Message communicating if the node were started successfully. */
    public static final String NODE_STARTED_MESSAGE = "project.cs.list.node.started";

    /** Activity context. */
    private static MainNetInfActivity sMainNetInfActivity;

    /** Toast for this activity. */
    private static Toast sToast;

    /** The menu. */
    private Menu menu;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Initializing the browser.");
        setContentView(R.layout.activity_main);

        sMainNetInfActivity = this;
        sToast = new Toast(this);

//        setupWifi();
        setupBluetoothAvailability();
        /*
         * TODO: Make sure that nothing happens when Netinf node doesn't start. 
         * Find a way to communicate between this activity and the netinf activity.
         */  
        setupBroadcastReceiver();

        // Get the input address
        EditText editText = (EditText) findViewById(R.id.url);
        editText.setText(UProperties.INSTANCE.getPropertyWithName("default.webpage"));

//        showDialog(new ShareDialog());


    }

    /**
     * Set up the WiFi connection.
     */
    private void setupWifi() {
        // Create OK dialog
        showDialog(new OkButtonDialog(
                "Wifi Information",
                getString(R.string.dialog_wifi_msg),
                new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "doPositiveClickWifiInfoMessage()");

                // This is run when OK is clicked
                // Create a WifiHandler
                WifiHandler wifiHandler = new WifiHandler() {
                    @Override
                    public void onDiscoveryDone(Set<String> wifis) {

                        // This is run when the WIFI discovery is done
                        // Create a ListDialog that shows the networks
                        ListDialog listDialog = new ListDialog(wifis) {
                            @Override
                            public void onConfirm(String wifi) {

                                // This is run when the ListDialog is confirmed
                                connectToSelectedNetwork(wifi);
                            }
                        };
                        showDialog(listDialog);
                    }
                };
                // Start WifiHandler discovery
                wifiHandler.startDiscovery();
            }
        }));
    }

    /**
     * Show a dialog.
     * @param dialog The dialog to show
     */
    private void showDialog(DialogFragment dialog) {
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "");
    }

    /**
     * Try to fetch the requested web page.
     * @param v The view that triggered this method
     */
    public final void goButtonClicked(final View v) {

        // get the web page address
        EditText editText = (EditText) findViewById(R.id.url);
        URL url = null;
        try {
            url = new URL(editText.getText().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            showToast("Malformed url!");
            return;
        }

        // Dismiss keyboard
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.requestFocus();

        if (!addressIsValid(url.toString())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Invalid url")
            .setTitle("Invalid url")
            .setNeutralButton("Ok, sorry :(", null);
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            // start downloading the web page
            FetchWebPageTask task = new FetchWebPageTask();
            task.execute(url);
        }
    }

    /**
     * Checks if a URL address is valid.
     * @param url   The url to validate
     * @return      if the given url is valid or not
     */
    public boolean addressIsValid(String url) {
        return url.matches(
                "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.activity_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_publish_file:
            item.setChecked(item.isChecked() ? false : true);
            break;
        default:
            break;
        }
        return true;
    }
    
    /**
     * Receives messages from the StarterNodeThread when the node is starter.
     * Right now it does not do anything. Just log
     */
    private void setupBroadcastReceiver() {
        Log.d(TAG, "setupBroadcastReceiver()");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                default:
                    Log.d(TAG, intent.getAction());
                    break;
                }
            }
        }, new IntentFilter(NODE_STARTED_MESSAGE));
    }

    /**
     * Function to forceably initialize Bluetooth and enable discoverability option.
     */
    private void setupBluetoothAvailability() {
        BTHandler bt = new BTHandler();
        bt.forceEnable(sMainNetInfActivity);
    }

    /**
     * Returns the context of this activity.
     * @return  the context
     */
    public static MainNetInfActivity getActivity() {
        return sMainNetInfActivity;
    }

    /**
     * Show a toast.
     * @param text      The text to show in the toast.
     */
    public static void showToast(String text) {
        Log.d(TAG, "showToast()");
        sToast.cancel();
        sToast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
        sToast.show();
    }

    /**
     * Cancel current toast.
     */
    public static void cancelToast() {
        Log.d(TAG, "cancelToast()");
        sToast.cancel();
    }
    
    public Menu getMenu() {
        return menu;
    }
}
