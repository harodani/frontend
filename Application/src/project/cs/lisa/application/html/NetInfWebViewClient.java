package project.cs.lisa.application.html;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import project.cs.lisa.application.http.RequestFailedException;
import project.cs.lisa.util.UProperties;
import project.cs.lisa.util.metadata.Metadata;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NetInfWebViewClient extends WebViewClient {
    
    /** Debugging tag. */
    private static final String TAG = "NetInfWebViewClient";
    
    /** Timeout for searching. */
    private static final int SEARCH_TIMEOUT = 
    		Integer.parseInt(UProperties.INSTANCE.getPropertyWithName("timeout.netinfsearch"));

    /** Timeout for retrieve. */
    private static final int RETRIEVE_TIMEOUT = 
    		Integer.parseInt(UProperties.INSTANCE.getPropertyWithName("timeout.netinfretrieve"));

    /** Message indicator for a URL change (i.e. a link was clicked). */
    public static final String URL_WAS_UPDATED = "new_url";

    /** The url extra field for the intent for URL updates. */
    public static final String URL = "url";

    /** The url extra field for the intent for URL updates. */
    public static final String FINISHED_LOADING_PAGE = "finished_loading_page";

    /** NetInf Restlet Address. */
    private static final String HOST = UProperties.INSTANCE.getPropertyWithName("access.http.host");

    /** NetInf Restlet Port. */
    private static final String PORT = UProperties.INSTANCE.getPropertyWithName("access.http.port");
    
    /** Hash Algorithm. */
    private static final String HASH_ALG = UProperties.INSTANCE.getPropertyWithName("hash.alg");

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Intent intent = new Intent(URL_WAS_UPDATED);
        intent.putExtra(URL, url);
        MainNetInfActivity.getActivity().sendBroadcast(intent);
        return false;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Intent intent = new Intent(FINISHED_LOADING_PAGE);
        MainNetInfActivity.getActivity().sendBroadcast(intent);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,
            String url) {

        Log.d("shouldInterceptRequest", url);

        /*
         * Ignore hashing raw data
         * String raw = "iVBORw0KGgoAAAANSUhEUgAAAFQAAABUCAYAAAAcaxDBAAADIUlEQVR42u3cwW3bMBQG4IygETSCNqgGKFAfe6sOvTcbxPce4g3iDeoNrA2sAQrYG1AbKFTyBDAySfFJpGlTf4D/EMSxrS8kRT7Seeq67gnxFyAAFKAARQAKUIAiAAUoQBGAAhSgAEUAClCAIgAFKEARgAIUoAhAAQpQgCIAvboYxlf1938l08pcZAqX3wGoHbRTcgCoX9AaoA6gEmpPYI1M5goqv9/QcNCnAugnSjlC+4JqAqWxVf1ZC9BPmGIEM6AWJlANZp8zQM2traNuPMauDY8V6gwANyU7qu37K0yATqPaInRzU4DOQxWmib4XUJ9fNHbVM1IyXmNLq5/Oc/obWnlvoI3mje4JYchedzGMP1gXMG1QULqAUkk20XJ0b7KcmD8O2d4BqAgG2uNpXnBjeGw50ZXU7t1YHps7oL7QOOgb8+S9y/cXpLTG7zJ/Rvk5arFDfss8e4jp+adSMG5MbfCbEnWlJnB3Cp0LrdFno3oBJcz2wTG/DEsaVOGC6gv0kBDmR0ul63JZerbel56JYQ7JGMWRSwqgZ5mjEt/PXzLKd+JRQXu4X6a5LI3lr56mRKVDgVlQqkcDPTOXlhnBBgMNugVyg1aZzVzGbha01jLaJl1ITIeWWDgsNcVM0Ip+93zTbeSA3TzTAPUrsZ1m3tuoY9nCWmfHGWJCgH4LkNxy8MAGURv+ENzXz6KdHBmV1nxks7CVNQZUznvIY4J6X/ppujl3abvXgJ44XT4ZUMthBG5yx3pr0qAnDejcwsuWUXNNFrR2KFK75gDQa9Ai0nOlCao5EsPJDi1UOXilQNSeZgvVKu/y42UercnZK62Fs4WkQHcajH9Llo50c2vXCtpqDr9mjEJyxdjzXwWotpU67KkfdVWhmSut5EA7U/WIkH4Q7gvt9+eWEt+c7e0kQa2ojpX7uTuyyYI6nVkyFJaXHLxIGnQ41VE5QOYLiimrAlVnAAe6az/THHVLVXyfR4FWA3qrRAUVqYFG/fCspzHrnnKMDZoxtxfuOR8fSIgKqsz53h64+wt6/3nwT6LgnwYAFKAARQAKUIAiAI2Qd1wqN+Bu1/KlAAAAAElFTkSuQmCC";
         * byte [] array_raw = Base64.decode(raw_, Base64.DEFAULT);
         * resource = new WebResourceResponse("image/jpg", "base64",
                    new ByteArrayInputStream(array_raw));
         */

        if (!url.startsWith("http")) {
        	Log.d(TAG, "Resource is already provided.");
            return null;
            
        } else if (url.startsWith("http")) {
        	Log.d(TAG, "Try to retrieve resource from url.");
        	File file = null;
        	String contentType = null;
        	
        	// Get and publish resource
			try {

        	// Search for url
        	NetInfSearchResponse searchResponse = search(url);
        	if (searchResponse == null) 
        		return null;
        	
        	// Get url data
        	String hash;
        	hash = selectHash(searchResponse);

        	NetInfRetrieveResponse retrieveResponse = retrieve(hash);
        	if (retrieveResponse == null)
        		return null;
   
        	file = retrieveResponse.getFile();
        	contentType = retrieveResponse.getContentType();
   
        	// Publish
            publish(file, new URL(url), hash, contentType);
            
			} catch (RequestFailedException e1) {
				Log.e(TAG, "Request for resource failed. Downloading from uplink.");
				return null;
			} catch (MalformedURLException e) {
				Log.e(TAG, "Malformed URL for resource. Could not publish.");
			}            
			
            WebResourceResponse resource = null;
            try {
                resource = new WebResourceResponse(contentType, "base64", FileUtils.openInputStream(file));
            } catch (IOException e) {
                Log.e("TAG", "Could not open file");
            }

            return resource;
        } else {
            Log.e(TAG, "Unexpected url while intercepting resources.");
            return super.shouldInterceptRequest(view, url);
        }
    }
    
	/**
     * Search for a URL and return a selected hash if one was found or null.
     * @param url The URL pointing to the resource in a web view.
     * @return The hash corresponding to the URL.
     */
    private NetInfSearchResponse search(String url) {
    	NetInfSearchResponse response;
        NetInfSearch search = new NetInfSearch(HOST, PORT, url.toString(), "empty");
        search.execute();
        
        try {
			response = (NetInfSearchResponse) search.get(SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Log.e(TAG, "Timeout was interrupted. Searching didn't finish.");
			return null;
		} catch (ExecutionException e) {
			Log.e(TAG, "Search failed.");
			return null;
		} catch (TimeoutException e) {
			Log.e(TAG, "Searching for object timed out.");
			return null;
		}
        return response;

    }

    private NetInfRetrieveResponse retrieve(String hash) {

    	NetInfRetrieveResponse response = null;
    	NetInfRetrieve retrieve = new NetInfRetrieve(HOST, PORT, HASH_ALG, hash);
    	retrieve.execute();
    	
		try {
			response = (NetInfRetrieveResponse) retrieve.get(RETRIEVE_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Log.e(TAG, "Timeout was interrupted. Retrieve didn't finish.");
			return null;
		} catch (ExecutionException e) {
			Log.e(TAG, "Retrieve failed.");
			return null;
		} catch (TimeoutException e) {
			Log.e(TAG, "Retrieve for object timed out.");
			return null;
		}
		return response;
	}

    private NetInfPublish createPublishRequest(File file, URL url, String hash, String contentType)
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
     * Executes a NetInf publish request.
     * @param file
     * @param url
     * @param hash
     * @param contentType
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

}
