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
package project.cs.lisa.application.html;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;

import project.cs.lisa.application.MainApplicationActivity;
import project.cs.lisa.application.html.transfer.DownloadWebObject;
import project.cs.lisa.application.html.transfer.WebObject;
import project.cs.lisa.application.http.Locator;
import project.cs.lisa.application.http.NetInfPublish;
import project.cs.lisa.application.http.NetInfRetrieve;
import project.cs.lisa.application.http.NetInfRetrieveResponse;
import project.cs.lisa.application.http.NetInfSearch;
import project.cs.lisa.application.http.NetInfSearchResponse;
import project.cs.lisa.application.http.RequestFailedException;
import project.cs.netinfutilities.UProperties;
import project.cs.netinfutilities.metadata.Metadata;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.URLUtil;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * The web view client that intercepts the web view in order
 * to use NetInf services when downloading web pages.
 * 
 * @author Paolo Boschini
 * @author Kim-Anh Tran
 *
 */
public class NetInfWebViewClient extends WebViewClient {

    /** Debugging tag. */
    private static final String TAG = "NetInfWebViewClient";

    /** Timeout for searching. */
    private static final int SEARCH_TIMEOUT = 
            Integer.parseInt(UProperties.INSTANCE.getPropertyWithName("timeout.netinfsearch"));

    /** Timeout for retrieve. */
    private static final int RETRIEVE_TIMEOUT = 
            Integer.parseInt(UProperties.INSTANCE.getPropertyWithName("timeout.netinfretrieve"));

    /** Timeout for downloading a Web Object. */
    private static final int DOWNLOAD_TIMEOUT = 
            Integer.parseInt(UProperties.INSTANCE.getPropertyWithName(
                    "timeout.netinfdownload.webobject"));

    /** Message indicator for a URL change (i.e. a link was clicked). */
    public static final String URL_WAS_UPDATED = "new_url";

    /** The url extra field for the intent for URL updates. */
    public static final String URL = "url";

    /** NetInf Restlet Address. */
    private static final String HOST = UProperties.INSTANCE.getPropertyWithName("access.http.host");

    /** NetInf Restlet Port. */
    private static final String PORT = UProperties.INSTANCE.getPropertyWithName("access.http.port");

    /** Hash Algorithm. */
    private static final String HASH_ALG = UProperties.INSTANCE.getPropertyWithName("hash.alg");

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        super.shouldOverrideUrlLoading(view, url);
        Intent intent = new Intent(URL_WAS_UPDATED);
        intent.putExtra(URL, url);
        MainApplicationActivity.getActivity().sendBroadcast(intent);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Intent intent = new Intent(MainApplicationActivity.FINISHED_LOADING_PAGE);
        MainApplicationActivity.getActivity().sendBroadcast(intent);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,
            String url) {

        // Log the current URL
        Log.e(TAG, "+++Getting url now: " + url);
        Intent intent = new Intent(MainApplicationActivity.SEARCH_TRANSMISSION);
        MainApplicationActivity.getActivity().sendBroadcast(intent);
        
        if (!URLUtil.isHttpUrl(url)) {
            super.shouldInterceptRequest(view, url);
            Log.e(TAG, "Request was not an HttpUrl. Downloading from uplink.");
            return null;

        } else if (URLUtil.isHttpUrl(url)) {
            
            WebObject resource = null;
            File file = null;
            String contentType = null;
            String hash = null;
            // Get and publish resource
            try {

                // Search for url
                NetInfSearchResponse searchResponse = search(url);

                // Get url data
                hash = selectHash(searchResponse);

                NetInfRetrieveResponse retrieveResponse = retrieve(hash);
                file = retrieveResponse.getFile();
                contentType = retrieveResponse.getContentType();

            } catch (Exception e1) {
                Log.e(TAG, "Request for resource failed. Downloading from uplink.");

                resource = downloadResource(url);
                if (resource == null) {
                    return null;
                }

                file = resource.getFile();
                contentType = resource.getContentType();
                hash = resource.getHash();
            }         


            // Publish
            try {
                if (shouldPublish()) {
                    publish(file, new URL(url), hash, contentType);
                }
            } catch (MalformedURLException e1) {
                Log.e(TAG, "Malformed url. Can't publish file.");
            }

            // Creating the resource that will be used by the webview
            WebResourceResponse response = null;
            try {
                response = new WebResourceResponse(
                        contentType, "base64", FileUtils.openInputStream(file));
            } catch (IOException e) {
                Log.e("TAG", "Could not open file");
            }

            return response;
        } else {
            Log.e(TAG, "Unexpected url while intercepting resources.");
            return super.shouldInterceptRequest(view, url);
        }
    }

    /**
     * Returns the downloaded WebObject specified by the url.
     * 
     * @param url	The url of the resource to download
     * @return		A web object representing the resource
     */
    private WebObject downloadResource(String url) {
        WebObject resource = null;
        DownloadWebObject downloadingResource = new DownloadWebObject();
        try {
            downloadingResource.execute(new URL(url));
            resource = downloadingResource.get(DOWNLOAD_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Failed retrieving resource from uplink.");
            resource = null;
        } 

        return resource;
    }

    /**
     * Searches for a URL and returns a selected hash if one was found or null.
     * @param url The URL pointing to the resource in a web view.
     * @return The hash corresponding to the URL.
     * @throws Exception Can throw several exceptions, but all belong to a search problem
     */
    private NetInfSearchResponse search(String url) throws Exception {
        NetInfSearchResponse response;
        NetInfSearch search = new NetInfSearch(url.toString(), "empty");
        search.execute();
        response = (NetInfSearchResponse) search.get(SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
        return response;

    }

    /**
     * Returns the response to a retrieve request that tries to get the
     * IO corresponding to the specified hash.
     * 
     * @param hash			The hash identifying the IO we want to retrieve.
     * @return				The response containing the IO
     * @throws Exception	Throws an exception that belongs to a retrieve process problem.
     */
    private NetInfRetrieveResponse retrieve(String hash) throws Exception {

        NetInfRetrieveResponse response = null;
        NetInfRetrieve retrieve = new NetInfRetrieve(HOST, PORT, HASH_ALG, hash);
        retrieve.execute();

        response = (NetInfRetrieveResponse) retrieve.get(RETRIEVE_TIMEOUT, TimeUnit.MILLISECONDS);

        return response;
    }

    /**
     * Returns a NetInfPublish request object that can be used in order
     * to publish an IO.
     * 
     * @param file			The file corresponding to the IO
     * @param url			The url where that file was downloaded from
     * @param hash			The hash identifying the file
     * @param contentType	The content type of the file
     * @return				Returns a publish request object
     * @throws IOException	Thrown, if Bluetooth is not available
     */
    private NetInfPublish createPublishRequest(File file, URL url, String hash, String contentType)
            throws IOException {

        // Create metadata
        Metadata metadata = new Metadata();
        metadata.insert("filesize", String.valueOf(file.length()));
        metadata.insert("filepath", file.getAbsolutePath());
        metadata.insert("time", Long.toString(System.currentTimeMillis()));
        metadata.insert("url", url.toString());

        // Try to get the Bluetooth MAC
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new IOException("Error: Bluetooth not supported");

        } else if (!adapter.isEnabled()) {
            throw new IOException("Error: Bluetooth not enabled");

        } else {
            // Create Locator set to be used in the publish
            HashSet<Locator> locators = new HashSet<Locator>();
            locators.add(new Locator(Locator.Type.BLUETOOTH, adapter.getAddress()));

            // Create the publish, adding locators, content type, and metadata
            NetInfPublish publishRequest = new NetInfPublish(HASH_ALG, hash, locators);
            publishRequest.setContentType(contentType);
            publishRequest.setMetadata(metadata);

            // Check for full put
            SharedPreferences sharedPref = 
                    PreferenceManager.getDefaultSharedPreferences(
                            MainApplicationActivity.getActivity().getApplicationContext());
            boolean isFullPutAvailable = sharedPref.getBoolean("pref_key_fullput", false);
            if (isFullPutAvailable) {
                publishRequest.setFile(file);
            }

            return publishRequest;
        }
    }

    /**
     * Executes a NetInf publish request.
     * 
     * @param file			The file that needs to be published
     * @param url			The url where the file can be downloaded
     * @param hash			The hash identifying the file
     * @param contentType	The content type of the file
     */
    private void publish(File file, URL url, String hash, String contentType) {
        NetInfPublish publish;
        try {
            publish = createPublishRequest(file, url, hash, contentType);
            publish.execute();
        } catch (IOException e) {
            Log.e(TAG, "Something went wrong with publish this resource.");
        }
    }

    /**
     * Selects an appropriate hash to download from a search result.
     * @param search
     *      The response to a NetInf search request
     * @return
     *      The selected hash
     * @throws RequestFailedException
     *      In case the search failed
     */
    private String selectHash(NetInfSearchResponse search) throws RequestFailedException {
        JSONObject firstResult = (JSONObject) search.getSearchResults().get(0);
        String address = (String) firstResult.get("ni");
        return address.split(";")[1];
    }
    
    /**
     * Returns true if the settings enable publishing.
     * @return  if we should publish or not.
     */
    private boolean shouldPublish() {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(
                        MainApplicationActivity.getActivity().getApplicationContext());
        return prefs.getBoolean("pref_key_publish", false);
    }
}