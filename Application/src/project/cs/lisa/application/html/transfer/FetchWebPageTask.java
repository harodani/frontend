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
package project.cs.lisa.application.html.transfer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;

import project.cs.lisa.application.MainApplicationActivity;
import project.cs.lisa.application.http.Locator;
import project.cs.lisa.application.http.NetInfPublish;
import project.cs.lisa.application.http.NetInfResponse;
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
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.URLUtil;
import android.webkit.WebView;

/**
 * Loads web pages asynchronously.
 * @author Paolo Boschini
 * @author Linus Sunde
 * @author Kim-Anh Tran
 *
 */
public class FetchWebPageTask extends AsyncTask<URL, Void, Void> {

    /** Debugging tag. */
    private static final String TAG = "FetchWebPageTask";

    /** ISO encoding indicator. */
    private static final String ISO_ENCODING = "iso-8859-1";

    /** UTF encoding indicator. */
    private static final String UTF8_ENCODING = "utf-8";

    /** Tags that help to find the encoding within an HTML page. */
    private static final String[] ENCODING_TAGS = {"encoding=", "charset="};

    /** NetInf Restlet Address. */
    private static final String HOST = UProperties.INSTANCE.getPropertyWithName("access.http.host");

    /** NetInf Restlet Port. */
    private static final String PORT = UProperties.INSTANCE.getPropertyWithName("access.http.port");

    /** Hash Algorithm. */
    private static final String HASH_ALG = UProperties.INSTANCE.getPropertyWithName("hash.alg");

    /** Web view to display the web page. */
    private WebView mWebView;

    /**
     * Default constructor.
     * 
     * @param   webView     The web view used for displaying web content.
     */
    public FetchWebPageTask(WebView webView) {
        mWebView = webView;
    }

    /**
     * Retrieves and displays a web page.
     * @param urls
     *      Exactly one URL, the web page to retrieve
     * @return
     *      Nothing
     */
    @Override
    protected Void doInBackground(URL... urls) {

        // Check arguments
        if (urls.length != 1) {
            Log.e(TAG, "FetchWebPageTask called with multiple URLs");
            return null;
        }

        Log.d(TAG, "Searching and retrieving " + urls[0].toString());
        searchRetrieveDisplay(urls[0]).execute();
        return null;
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

    /**
     * Creates a new task that searches for and tries to retrieve a URL using NetInf.
     * If the NetInf requests fail, it tries to fetch the URL from the Internet.
     * The page is then displayed.
     * @param url
     *      The URL to search, retrieve, and display
     * @return
     *      The created task
     */
    private NetInfSearch searchRetrieveDisplay(final URL url) {
        
        Runtime.getRuntime().gc();

        return new NetInfSearch(url.toString(), "empty") {
            @Override
            public void onPostExecute(NetInfResponse response) {

                NetInfSearchResponse search = (NetInfSearchResponse) response;

                try {
                    // Assume search succeeded, select a hash from the results and retrieve it
                    String hash = selectHash(search);
                    retrieveDisplay(url, hash).execute();
                } catch (RequestFailedException e) {
                    // If the search failed for any reason, use uplink
                    Log.d(TAG, "Downloading web page because search failed.");
                    downloadAndDisplay(url).execute(url);
                }

            }
        };
    }

    /**
     * Creates a new task that tries to retrieve a hash associated with a URL using NetInf.
     * If the NetInf request fail, it tries to fetch the URL from the Internet.
     * The page is then displayed.
     * @param url
     *      The URL to retrieve, and display
     * @param hash
     *      The hash of the URL
     * @return
     *      The created task
     */
    private NetInfRetrieve retrieveDisplay(final URL url, final String hash) {
        return new NetInfRetrieve(HOST, PORT, HASH_ALG, hash) {
            @Override
            protected void onPostExecute(NetInfResponse response) {

                NetInfRetrieveResponse retrieve = (NetInfRetrieveResponse) response;

                try {
                    // Assume retrieve succedded, display page and publish
                    displayWebpage(retrieve.getFile(), url.getHost(), retrieve.getContentType());
                    try {
                        if (shouldPublish()) {
                            publish(retrieve.getFile(), url, hash, retrieve.getContentType())
                            .execute();
                        }
                    } catch (IOException e) {
                        MainApplicationActivity.showToast(e.getMessage());
                    }
                } catch (RequestFailedException e) {
                    // If the retrieve failed for any reason, use uplink
                    downloadAndDisplay(url).execute(url);
                }

            }
        };
    }

    /**
     * Creates a new task that downloads and displays a URL.
     * @param url
     *      The URL to download
     * @return
     *      The created task
     */
    private DownloadWebObject downloadAndDisplay(final URL url) {
        return new DownloadWebObject() {
            @Override
            protected void onPostExecute(WebObject webObject) {

                if (webObject == null) {
                    MainApplicationActivity.showToast(
                            "Download failed. Check internet connection.");
                    Intent intent = new Intent(MainApplicationActivity.FINISHED_LOADING_PAGE);
                    MainApplicationActivity.getActivity().sendBroadcast(intent);
                    return;
                }

                File file = webObject.getFile();
                String hash = webObject.getHash();
                String contentType = webObject.getContentType();

                displayWebpage(file, url.getHost(), contentType);
                try {
                    if (shouldPublish()) {
                        publish(file, url, hash, contentType).execute();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Could not publish file.");
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * Creates a new task that tries to publish a file downloaded
     * associated with a URL with a given hash and content type.
     * The publish is done with your Bluetooth as a locator.
     * @param file
     *      The file
     * @param url
     *      The associated URL
     * @param hash
     *      The hash of the file
     * @param contentType
     *      The content type of the file
     * @return
     *      The created task
     * @throws IOException
     *      In case Bluetooth was not available
     */
    private NetInfPublish publish(File file, URL url, String hash, String contentType)
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

            // Check for fullput
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(
                    MainApplicationActivity.getActivity().getApplicationContext());
            boolean isFullPutAvailable = sharedPref.getBoolean("pref_key_fullput", false);

            if (isFullPutAvailable) {
                publishRequest.setFile(file);
            }

            return publishRequest;
        }
    }

    /**
     * Selects an appropriate hash to download from a search result.
     * @param search
     *      The response to a NetInf search request
     * @return
     *      The selected hash
     * @throws RequestFailedException
     *      In case the seach failed
     */
    private String selectHash(NetInfSearchResponse search) throws RequestFailedException {
        JSONObject firstResult = (JSONObject) search.getSearchResults().get(0);
        String address = (String) firstResult.get("ni");
        return address.split(";")[1];
    }

    /**
     * Show a HTML in the web view.
     * 
     * @param webPage
     *      The web page
     */
    private void displayWebpage(File webPage, String baseUrl, String contentType) {
        if (webPage == null) {
            Log.e(TAG, "Webpage was null. Can't display webpage.");
            MainApplicationActivity.showToast("Could not download web page.");
            return;
        }

        try {
            if (!URLUtil.isHttpUrl(baseUrl)) {
                baseUrl = "http://" + baseUrl;
            }

            // Detect encoding
            String html;
            String encoding;
            String encodingIndicator = ";";
            if (contentType.contains(encodingIndicator)) { 
                int encodingIndicatorIndex = contentType.indexOf(encodingIndicator);

                String encodingStartIndicator = "=";
                int startIndex = contentType.indexOf(encodingStartIndicator, encodingIndicatorIndex)
                        + encodingIndicator.length();
                encoding = contentType.substring(startIndex);        		
                html = FileUtils.readFileToString(webPage, encoding);

            } else {
                /*
                 *  Read in webpage. First assume iso-8859-1 encoding 
                 *  and then detect encoding from String
                 */
                html = FileUtils.readFileToString(webPage, ISO_ENCODING);

                encoding = getEncoding(html);
                if (!encoding.isEmpty() 
                        && !encoding.toLowerCase(Locale.ENGLISH).equals(ISO_ENCODING)) {
                    html = FileUtils.readFileToString(webPage, encoding);
                }

            }

            /*
             *  Independent on the actual encoding, we need to specify 
             *  utf-8 within the load webpage call.
             */
            mWebView.loadDataWithBaseURL(baseUrl, html, "text/html", UTF8_ENCODING, null);
        } catch (IOException e) {
            MainApplicationActivity.showToast("Could not load web page.");
        }
    }

    /**
     * Returns the encoding of the webpage based on the html code, if existent.
     * Returns an empty String if no encoding indicator was found.
     * 
     * @param html	The html String
     * @return		The encoding, if found
     */
    private String getEncoding(String html) {
        /* Check for: 
         * <?xml version="1.0" encoding="UTF-8"?>
         * <meta charset="UTF-8"> 
         * <meta http-equiv="Content-type" content="text/html;charset=UTF-8">
         */

        String encoding = "";
        char encodingDelimiter = '\"';
        int startIndex;
        int endIndex;

        for (String indicator : ENCODING_TAGS) {
            if (html.contains(indicator)) {
                startIndex = html.indexOf(indicator) + indicator.length();

                if (html.charAt(startIndex) == encodingDelimiter) {
                    ++startIndex;
                }

                endIndex = html.indexOf(encodingDelimiter, startIndex);
                encoding = html.substring(startIndex, endIndex);
            }
        }

        return encoding;

    }
}
