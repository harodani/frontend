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
import project.cs.lisa.application.html.NetInfWebViewClient;
import project.cs.lisa.application.html.transfer.FetchWebPageTask;
import project.cs.lisa.networksettings.BTHandler;
import project.cs.lisa.networksettings.WifiHandler;
import project.cs.lisa.util.UProperties;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
public class MainApplicationActivity extends BaseMenuActivity {

    /** Debugging tag. */
    private static final String TAG = "MainApplicationActivity";

    /** Message communicating if the node were started successfully. */
    public static final String NODE_STARTED_MESSAGE = "project.cs.lisa.node.started";

    /** The url extra field for the intent for URL updates. */
    public static final String FINISHED_LOADING_PAGE = "finished_loading_page";

    /** Bluetooth transmission used to transfer a resource. */
    public static final String BLUETOOTH_TRANSMISSION = "project.cs.netinfservice.BLUETOOTH_TRANSMISSION";

    /** Local File system (database) transmission used to transfer a resource. */
    public static final String LOCAL_TRANSMISSION = "project.cs.netinfservice.LOCAL_TRANSMISSION";

    /** Uplink transmission used to transfer a resource. */
    public static final String UPLINK_TRANSMISSION = "project.cs.lisa.UPLINK_TRANSMISSION";

    /** NRS cache transmission used to transfer a resource. */
    public static final String NRS_TRANSMISSION = "project.cs.netinfservice.NRS_TRANSMISSION";

    /** Activity context. */
    private static MainApplicationActivity sMainNetInfActivity;

    /** Broadcast receiver. */
    private BroadcastReceiver mBroadcastReceiver;

    /** Intent for Broadcast receiver. */
    private IntentFilter mIntentFilter;

    /** Toast for this activity. */
    private static Toast sToast;

    /** The menu. */
    private Menu mMenu;

    /** The main web view. */
    private WebView mWebView;

    /** The URL search bar. */
    private EditText mEditText;

    /** Icon imageview for the url search bar. */
    private ImageView img;

    /** Spinning progress bar, shown when loading a page. */
    private ProgressBar mSpinningBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Initializing the browser.");

//        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("project.cs.netinfservice");
//        startActivity(LaunchIntent);

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
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(NODE_STARTED_MESSAGE);
        mIntentFilter.addAction(NetInfWebViewClient.URL_WAS_UPDATED);
        mIntentFilter.addAction(FINISHED_LOADING_PAGE);
        mIntentFilter.addAction(BLUETOOTH_TRANSMISSION);
        mIntentFilter.addAction(LOCAL_TRANSMISSION);
        mIntentFilter.addAction(UPLINK_TRANSMISSION);
        mIntentFilter.addAction(NRS_TRANSMISSION);
        registerReceiver(mBroadcastReceiver, mIntentFilter);


        // Get the input address
        mEditText = (EditText) findViewById(R.id.url);
        mEditText.setText(UProperties.INSTANCE.getPropertyWithName("default.webpage"));
        mEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    startFetchingWebPage();
                    return true;
                }
                return false;
            }
        });


        img = (ImageView) findViewById(R.id.imageView);
        img.setImageResource(R.drawable.refresh);
        img.setTag(R.drawable.refresh);
        img.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int tag = (Integer)img.getTag();
                switch (tag) {
                case R.drawable.refresh:
                    img.setImageResource(R.drawable.cancel);
                    img.setTag(R.drawable.cancel);
                    startFetchingWebPage();
                    break;
                case R.drawable.cancel:
                    img.setImageResource(R.drawable.refresh);
                    img.setTag(R.drawable.refresh);
                    mSpinningBar.setVisibility(View.INVISIBLE);
                    mWebView.stopLoading();
                default:
                    break;
                }
            }
        });

        mWebView = (WebView) findViewById(R.id.webView);
        // mWebView: enable pinch zooming
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.setWebViewClient(new NetInfWebViewClient());

        mSpinningBar = (ProgressBar) findViewById(R.id.progressBar);
        mSpinningBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_grey));
        mSpinningBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_grey));
        mSpinningBar.setVisibility(View.INVISIBLE);

        //        showDialog(new ShareDialog());


    }

    private class WifiDialogListener implements DialogInterface.OnClickListener {
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
    }

    // TODO Please don't delete yet
    //    public void debug() {
    //        // DEBUG
    //        NetInfSearch search = null;
    //            search = new NetInfSearch("localhost", "8080", "http://support.google.com/richmedia/bin/answer.py?hl=en&answer=1100953&ctx=cb&src=cb&cbid=100pnperzakdj&cbrank=5", "empty") {
    //                @Override
    //                protected void onPostExecute(NetInfResponse response) {
    //                     NetInfSearchResponse searchResponse = (NetInfSearchResponse) response;
    //                     Log.d("DEBUG", searchResponse.getStatus().toString());
    //                     try {
    //                        Log.d("DEBUG", searchResponse.getSearchResults().toString());
    //                    } catch (RequestFailedException e) {
    //                        Log.d("DEBUG", "Search failed :(");
    //                    }
    //                }
    //            };
    //        search.execute();
    //    }

    /**
     * Set up the WiFi connection.
     */
    private void setupWifi() {
        showDialog(new OkButtonDialog(
                "Wifi Information",
                getString(R.string.dialog_wifi_msg),
                new WifiDialogListener()));

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
     * Called when the user opens a web page.
     * @param v The view that triggered this method
     */
    public final void startFetchingWebPage() {

        // get the web page address
        URL url = null;
        try {
            String inputUrl = mEditText.getText().toString();
            url = new URL(URLUtil.guessUrl(inputUrl));
            mEditText.setText(url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            showToast("Malformed url!");
            return;
        }

        // Dismiss keyboard
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        mWebView.requestFocus();

        if (!addressIsValid(url.toString())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Invalid url")
            .setTitle("Invalid url")
            .setNeutralButton("Ok, sorry :(", null);
            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            // start downloading the web page
            mSpinningBar.setVisibility(View.VISIBLE);
            FetchWebPageTask task = new FetchWebPageTask(mWebView);
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

    /**
     * Receives messages from the StarterNodeThread when the node is starter.
     * Right now it does not do anything. Just log
     */
    private void setupBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "action was: " + action);

                if (action.equals(NetInfWebViewClient.URL_WAS_UPDATED)) {
                    String newUrl = (String) intent.getExtras().get(NetInfWebViewClient.URL);
                    mEditText.setText(newUrl);
                    startFetchingWebPage();

                } else if (action.equals(FINISHED_LOADING_PAGE)) {
                    mSpinningBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_grey));
                    mSpinningBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_grey));
                    mSpinningBar.setVisibility(View.INVISIBLE);
                    img.setImageResource(R.drawable.refresh);
                    img.setTag(R.drawable.refresh);

                } else if (action.equals(NODE_STARTED_MESSAGE)) {
                    Log.d(TAG, "The NetInf node was started.");

                } else if (action.equals(BLUETOOTH_TRANSMISSION)) {
                    Log.d(TAG, "Trasferring resource using Bluetooth");
                    mSpinningBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_blue));
                    mSpinningBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_blue));

                } else if (action.equals(LOCAL_TRANSMISSION)) {
                    Log.d(TAG, "Trasferring resource using local file system");
                    mSpinningBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_green));
                    mSpinningBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_green));

                } else if (action.equals(UPLINK_TRANSMISSION)) {
                    Log.d(TAG, "Trasferring resource using uplink");
                    mSpinningBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_red));
                    mSpinningBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_red));

                } else if (action.equals(NRS_TRANSMISSION)) {
                    Log.d(TAG, "Trasferring resource using nrs cache");
                    mSpinningBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_black));
                    mSpinningBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_black));
                }
            }
        };
    }

    /**
     * Function to frceably initialize Bluetooth and enable discoverability option.
     */
    private void setupBluetoothAvailability() {
        BTHandler bt = new BTHandler();
        bt.forceEnable(sMainNetInfActivity);
    }

    /**
     * Returns the context of this activity.
     * @return  the context
     */
    public static MainApplicationActivity getActivity() {
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
        return mMenu;
    }

}
