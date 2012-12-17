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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import project.cs.lisa.application.MainApplicationActivity;
import project.cs.lisa.application.hash.Hash;
import project.cs.lisa.application.log.LogEntry;
import project.cs.netinfutilities.UProperties;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * Downloads a web resource from the web via uplink.
 * @author Paolo Boschini
 * @author Linus Sunde
 * @author Kim-Anh Tran
 *
 */
public class DownloadWebObject extends AsyncTask<URL, Void, WebObject> {

    /** Debugging tag. */
    private static final String TAG = "DownloadWebObject";

    /** The directory containing the published files. */
    private String mSharedFolder;

    /** Uplink transmission used to transfer a resource. */
    public static final String UPLINK_TRANSMISSION = "project.cs.lisa.UPLINK_TRANSMISSION";

    /** Buffer size for reading the input stream. */
    public static final int BUFFER_SIZE = 1024;

    /**
     * Default constructor.
     * Gets the shared folder to save files to.
     */
    public DownloadWebObject() {
        String relativeFolderPath = UProperties.INSTANCE.getPropertyWithName("sharing.folder");
        mSharedFolder = Environment.getExternalStorageDirectory() + relativeFolderPath;
    }

    @Override
    protected WebObject doInBackground(URL... urls) {
        URL url = urls[0];
        WebObject webObject = null;
        try {
            webObject = downloadWebObject(url);
        } catch (IOException e) {
            Log.e(TAG, "Could NOT download URL from uplink: " + url);
        }
        return webObject;
    }

    /**
     * Checks for Internet connection.
     * @return If Internet is available or not
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) MainApplicationActivity.getActivity().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        }
        return true;
    }

    /**
     * Downloads a web object and saves it to file.
     * @param url
     *      The URL of the web object to download
     * @return
     *      A file containing the downloaded web page
     * @throws IOException
     *      In case the web object could not be downloaded and saved
     */
    private WebObject downloadWebObject(URL url) throws IOException {

        if (!isNetworkConnected()) {
        	Log.e(TAG, "No network connection.");
            return null;
        }

        Intent intent = new Intent(UPLINK_TRANSMISSION);
        MainApplicationActivity.getActivity().sendBroadcast(intent);

        LogEntry logEntry = new LogEntry(LogEntry.Type.UPLINK, LogEntry.Action.GET_WITH_FILE);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String contentType = connection.getContentType();
        if (contentType == null) {
        	contentType = "unknown";
        }

        // Returns null when the the page is not found
        //InputStream is = representation.getStream();
        InputStream is = connection.getInputStream();
        if (is == null) {
            return null;
		}

        byte[] bytes = null;
        try {
        	bytes = extract(is);
        } catch (IOException e) {
        	Log.e(TAG, "Error occured. Could not find the resource.");
        	e.printStackTrace();
        	logEntry.failed();
        	throw new IOException("Error occured. Could not find the resource.");
        }

        String hash = hashContent(bytes);

        logEntry.done(bytes);

        File file = new File(mSharedFolder + hash);
        FileUtils.writeByteArrayToFile(file, bytes);

        WebObject webObject = new WebObject(contentType, file, hash);
        return webObject;
    }

    /**
     * Extracts bytes from an input stream.
     * @param inputStream   the input stream
     * @return              the bytes from the input stream
     * @throws IOException  Thrown if something goes wrong with the extraction
     */
    private byte[] extract(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
			baos.write(buffer, 0, read);
		}
		baos.flush();
		return baos.toByteArray();
	}

    /**
     * Hashes data.
     * @param bytes     The data
     * @return          The hash
     */
    private String hashContent(byte[] bytes) {
        Hash hash = null;
        String result = null;

        hash = new Hash(bytes);
        result = hash.encodeResult();

        return result;
    }
}