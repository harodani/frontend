package project.cs.lisa.application.html.transfer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;

import project.cs.lisa.R;
import project.cs.lisa.application.MainNetInfActivity;
import project.cs.lisa.application.http.Locator;
import project.cs.lisa.application.http.NetInfPublish;
import project.cs.lisa.application.http.NetInfResponse;
import project.cs.lisa.application.http.NetInfRetrieve;
import project.cs.lisa.application.http.NetInfRetrieveResponse;
import project.cs.lisa.application.http.NetInfSearch;
import project.cs.lisa.application.http.NetInfSearchResponse;
import project.cs.lisa.application.http.NetInfStatus;
import project.cs.lisa.netinf.node.metadata.Metadata;
import project.cs.lisa.util.UProperties;
import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

/**
 * Loads the web page asynchronously.
 * @author Paolo Boschini
 * @author Linus Sunde
 *
 */
public class FetchWebPageTask extends AsyncTask<URL, Void, Void> {

    /** Debugging tag. */
    private static final String TAG = "FetchWebPageTask";

    /** NetInf Restlet Address. */
    private static final String HOST = UProperties.INSTANCE.getPropertyWithName("access.http.host");

    /** NetInf Restlet Port. */
    private static final String PORT = UProperties.INSTANCE.getPropertyWithName("access.http.port");

    /** Hash Algorithm. */
    private static final String HASH_ALG = UProperties.INSTANCE.getPropertyWithName("hash.alg");

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
                    MainNetInfActivity.showToast("Download failed. Check internet connection.");
                    return;
                }

                File file = webObject.getFile();
                String hash = webObject.getHash();
                String contentType = webObject.getContentType();

                displayWebpage(file);
                try {
                    publish(file, url, hash, contentType).execute();
                } catch (IOException e) {
                    Log.e(TAG, "Could not publish file.");
                    e.printStackTrace();
                }
            }
        };
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
        return new NetInfSearch(HOST, PORT, url.toString(), "empty") {
            @Override
            public void onPostExecute(NetInfResponse response) {

                NetInfSearchResponse search = (NetInfSearchResponse) response;

                // If the search failed for any reason, use uplink
                if (search.getStatus() != NetInfStatus.OK) {
                    Log.d(TAG, "Downloading web page because search failed: " + search.getStatus());
                    downloadAndDisplay(url).execute(url);
                    return;
                }

                // Search succeeded, select a hash from the results and retrieve it
                String hash = selectHash(search);
                retrieveDisplay(url, hash).execute();
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

                // If retrieved failed for any reason, use uplink
                if (retrieve.getStatus() != NetInfStatus.OK) {
                    downloadAndDisplay(url).execute(url);
                    return;
                }

                // Retrieve succeeded, display page and publish
                displayWebpage(retrieve.getFile());
                try {
                    publish(retrieve.getFile(), url, hash, retrieve.getContentType()).execute();
                } catch (IOException e) {
                    MainNetInfActivity.showToast(e.getMessage());
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

        Log.d(TAG, "Trying to publish a new file.");

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
            NetInfPublish publishRequest = new NetInfPublish(HOST, PORT, HASH_ALG, hash, locators);
            publishRequest.setContentType(contentType);
            publishRequest.setMetadata(metadata);

            // Check for fullput
            Menu menu = (Menu) MainNetInfActivity.getActivity().getMenu();
            MenuItem fullPut = menu.findItem(R.id.menu_publish_file);
            if (fullPut.isChecked()) {
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
     */
    private String selectHash(NetInfSearchResponse search) {
        JSONObject firstResult = (JSONObject) search.getSearchResults().get(0);
        String address = (String) firstResult.get("ni");
        return address.split(";")[1];
    }

    /**
     * Show a HTML in the web view.
     * @param webPage
     *      The web page
     */
    private void displayWebpage(File webPage) {
        if (webPage == null) {
            Log.d(TAG, "webPage == null");
            MainNetInfActivity.showToast("Could not download web page.");
            return;
        }
        try {
            String result = FileUtils.readFileToString(webPage);
            WebView webView = (WebView) MainNetInfActivity.getActivity().findViewById(R.id.webView);
            webView.loadDataWithBaseURL(result, result, "text/html", null, null);
        } catch (IOException e) {
            MainNetInfActivity.showToast("Could not load web page.");
        }
    }
}
