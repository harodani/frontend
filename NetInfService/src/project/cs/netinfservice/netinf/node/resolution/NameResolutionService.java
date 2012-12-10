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
package project.cs.netinfservice.netinf.node.resolution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.node.resolution.ResolutionService;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import project.cs.netinfservice.application.MainNetInfActivity;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedAttributeIdentification;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.netinfservice.netinf.node.exceptions.InvalidResponseException;
import project.cs.netinfservice.util.UProperties;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A resolution service implementation that uses the HTTP convergence layer to a specific NRS.
 * @author Linus Sunde
 * @author Harold Martinez
 * @author Thiago Costa Porto
 *
 */
public class NameResolutionService
extends AbstractResolutionServiceWithoutId
implements ResolutionService {

    /** Debug tag. **/
    public static final String TAG = "NameResolutionService";

    /** Message ID random value max. **/
    public static final int MSG_ID_MAX = Integer
    		.parseInt(UProperties.INSTANCE.getPropertyWithName("nrs.max_messsage"));

    /** HTTP Scheme. */
    private static final String HTTP = "http://";

    /** NRS IP address. **/
    private String mHost;

    /** NRS port. **/
    private int mPort;

    /** HTTP connection timeout. **/
    private static final int TIMEOUT = Integer
    		.parseInt(UProperties.INSTANCE.getPropertyWithName("nrs.timeout"));

    /** Implementation of DatamodelFactory, used to create and edit InformationObjects etc. **/
    private final DatamodelFactory mDatamodelFactory;

    /** Random number generator used to create message IDs. **/
    private final Random mRandomGenerator = new Random();

    /** NRS cache transmission used to transfer a resource. */
    public static final String NRS_TRANSMISSION = "project.cs.netinfservice.NRS_TRANSMISSION";

    /** HTTP Client. **/
    private HttpClient mClient;

    /** Key for accessing the NRS IP. */
	private static final String PREF_KEY_NRS_IP = "pref_key_nrs_ip";

	/** Key for accessing the NRS Port. */
	private static final String PREF_KEY_NRS_PORT = "pref_key_nrs_port";

    /**
     * Creates a new Name Resolution Service that communicates with a specific NRS.
     * @param host                 The NRS IP Address
     * @param port                 The NRS Port
     * @param datamodelFactory     Creates different objects necessary in the NetInf model
     */
    @Inject
    public NameResolutionService(
            @Named("nrs.http.host") String host,
            @Named("nrs.http.port") int port,
            DatamodelFactory datamodelFactory) {
        // Setup HTTP client
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        mClient = new DefaultHttpClient(params);

        mHost = host;
        mPort = port;
        mDatamodelFactory = datamodelFactory;
    }



    /**
     * Get the NRS Address.
     * @return the IP Address of the NRS
     */
    private String getHost() {
    	SharedPreferences sharedPreferences =
    			PreferenceManager.getDefaultSharedPreferences(MainNetInfActivity.getActivity());
    	mHost = sharedPreferences.getString(PREF_KEY_NRS_IP, mHost);
    	return mHost;

	}



    /**
     * Get the NRS port.
     * @return the port of the NRS
     */
    private int getPort() {
    	SharedPreferences sharedPreferences =
    			PreferenceManager.getDefaultSharedPreferences(MainNetInfActivity.getActivity());
		mPort = Integer.parseInt(sharedPreferences
				.getString(PREF_KEY_NRS_PORT, Integer.toString(mPort)));
		return mPort;

	}



    @Override
    public void delete(Identifier arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public String describe() {
        return "backend NRS";
    }

    /**
     * Gets the hash algorithm from an identifier.
     * @param identifier   The identifier
     * @return             The hash algorithm
     */
    private String getHashAlg(Identifier identifier) {
        String hashAlg = identifier.getIdentifierLabel(
                SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();
        return hashAlg;
    }

    /**
     * Gets the hash from an identifier.
     * @param identifier   The identifier
     * @return             The hash
     */
    private String getHash(Identifier identifier) {
        String hash = identifier.getIdentifierLabel(
                SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
        return hash;
    }

    /**
     * Gets the content-type from an identifier.
     * @param identifier   The identifier
     * @return             The content-type
     */
    private String getContentType(Identifier identifier) {
        String contentType = identifier.getIdentifierLabel(
                SailDefinedLabelName.CONTENT_TYPE.getLabelName()).getLabelValue();
        return contentType;
    }

    /**
     * Gets the metadata from an identifier.
     * @param identifier   The identifier
     * @return             The metadata
     */
    private String getMetadata(Identifier identifier) {
        String metadata = identifier.getIdentifierLabel(
                SailDefinedLabelName.META_DATA.getLabelName()).getLabelValue();
        return metadata;
    }

    /**
     * Gets the file path from an InformationObject.
     * @param io
     *      The information object
     * @return
     *      The file path
     */
    private String getFilePath(InformationObject io) {
        Attribute filepathAttribute =
                io.getSingleAttribute(SailDefinedAttributeIdentification.FILE_PATH.getURI());
        String filepath = null;
        if (filepathAttribute != null) {
            filepath = filepathAttribute.getValueRaw();
            filepath = filepath.substring(filepath.indexOf(":") + 1);
        }
        return filepath;
    }

    /**
     * Gets the first bluetooth locator from an InformationObject.
     * @param io
     *      The information object
     * @return
     *      The bluetooth locator
     */
    private String getBluetoothMac(InformationObject io) {
        Attribute bluetoothAttribute =
                io.getSingleAttribute(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
        String bluetoothLocator = null;
        if (bluetoothAttribute != null) {
            bluetoothLocator = bluetoothAttribute.getValueRaw();
            bluetoothLocator = bluetoothLocator.substring(bluetoothLocator.indexOf(":") + 1);
        }
        return bluetoothLocator;
    }

    /**
     * Reads the next content stream from a HTTP response, expecting it to be JSON.
     * @param response                     The HTTP response
     * @return                             The read JSON
     * @throws InvalidResponseException    In case reading the JSON failed
     */
    private String readJson(HttpResponse response) throws InvalidResponseException {
        if (response == null) {
            throw new InvalidResponseException("Response is null.");
        } else if (response.getEntity() == null) {
            throw new InvalidResponseException("Entity is null.");
            // TODO seems like content-type is not set
//        } else if (response.getEntity().getContentType() == null) {
//            throw new InvalidResponseException("Content-Type is null.");
//        } else if (!response.getEntity().getContentType().getValue().equals("application/json")) {
//            throw new InvalidResponseException("Content-Type is "
//                    + response.getEntity().getContentType().getValue()
//                    + ", expected \"application/json\"");
        }
        try {
            String jsonString = streamToString(response.getEntity().getContent());
            return jsonString;
        } catch (IOException e)  {
            throw new InvalidResponseException("Failed to convert stream to string.", e);
        }
    }

    /**
     * Converts the JSON String returned in the HTTP response into a JSONObject.
     * @param jsonString                   The JSON String from the HTTP response
     * @return                             The JSONObject
     * @throws InvalidResponseException    In case the JSON String is invalid
     */
    private JSONObject parseJson(String jsonString) throws InvalidResponseException {
        JSONObject json = (JSONObject) JSONValue.parse(jsonString);
        if (json == null) {
            Log.e(TAG, "Unable to parse JSON");
            Log.e(TAG, "jsonString = " + jsonString);
            throw new InvalidResponseException("Unable to parse JSON.");
        }
        return json;
    }

    /**
     * If the JSON contains a content-type, extract it and set the content-type of the identifier.
     * @param identifier       The identifier
     * @param json             The JSON
     */
    private void addContentType(Identifier identifier, JSONObject json) {

        // Check that the content-type is a string
        Object object = json.get("ct");
        if (!(object instanceof String)) {
            Log.w(TAG, "Content-Type NOT added.");
            return;
        }
        String contentType = (String) object;

        // Add the content-type
        IdentifierLabel label = mDatamodelFactory.createIdentifierLabel();
        label.setLabelName(SailDefinedLabelName.CONTENT_TYPE.getLabelName());
        label.setLabelValue(contentType);
        identifier.addIdentifierLabel(label);
    }

    /**
     * If the JSON contains metadata, extract it and set the metadata of the identifier.
     * @param identifier       The identifier
     * @param json             The JSON
     */
    private void addMetadata(Identifier identifier, JSONObject json) {

        // Check that the metadata is an JSONObject
        Object object = json.get("metadata");
        if (!(object instanceof JSONObject)) {
            Log.w(TAG, "Metadata NOT added.");
        }
        JSONObject metadata = (JSONObject) object;

        // Add the metadata
        IdentifierLabel label = mDatamodelFactory.createIdentifierLabel();
        label.setLabelName(SailDefinedLabelName.META_DATA.getLabelName());
        label.setLabelValue(metadata.toJSONString());
        identifier.addIdentifierLabel(label);
    }

    /**
     * If the JSON contains locators, extract them and add them to the InformationObject.
     * @param io               The InformationObject
     * @param json             The JSON
     */
    private void addLocators(InformationObject io, JSONObject json) {
        JSONArray locators = (JSONArray) json.get("loc");

        for (Object locator : locators) {

            String loc = (String) locator;

            Attribute newLocator = mDatamodelFactory.createAttribute();
            newLocator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            newLocator.setIdentification(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
            newLocator.setValue(locator);

            io.addAttribute(newLocator);
        }
    }

    /**
     * Create an IO based in an identifier and a HTTP Response.
     * @param identifier					the IO identifier
     * @param response						the HTTP Response
     * @return								a new IO
     * @throws InvalidResponseException		if the response is not correct
     */
    private InformationObject readIo(Identifier identifier, HttpResponse response)
            throws InvalidResponseException {
        InformationObject io = mDatamodelFactory.createInformationObject();
        io.setIdentifier(identifier);
        Log.w(TAG, "reading json");
        String jsonString = readJson(response);
        Log.w(TAG, jsonString);
        Log.w(TAG, "parsing json");
        JSONObject json = parseJson(jsonString);
        Log.w(TAG, json.toString());
        addContentType(identifier, json);
        addMetadata(identifier, json);
        addLocators(io, json);
        return io;
    }

    /**
     *
     * @param identifier
     * @param response
     * @return
     * @throws InvalidResponseException
     */
    private InformationObject readIoAndFile(Identifier identifier,
            HttpResponse response) throws InvalidResponseException {

        if (response == null) {
            throw new InvalidResponseException("Response is null.");
        } else if (response.getEntity() == null) {
            throw new InvalidResponseException("Entity is null.");
        } else if (response.getEntity().getContentType() == null) {
            throw new InvalidResponseException("Content-Type is null.");
        } else if (!response.getEntity().getContentType().getValue().startsWith("multipart/form-data")) {
            throw new InvalidResponseException("Content-Type is "
                    + response.getEntity().getContentType().getValue()
                    + ", expected to start with \"multipart/form-data\"");
        }

        try {

            // Create IO
            InformationObject io = mDatamodelFactory.createInformationObject();
            io.setIdentifier(identifier);

            String contentType = response.getHeaders("Content-Type")[0].getValue();

            byte[] boundary = (contentType.substring(contentType.indexOf("boundary=") + 9)).getBytes();

            Log.d(TAG, "Sending Intent " + NRS_TRANSMISSION);
            Intent intent = new Intent(NRS_TRANSMISSION);
            MainNetInfActivity.getActivity().sendBroadcast(intent);

            @SuppressWarnings("deprecation")
            MultipartStream multipartStream =
                    new MultipartStream(response.getEntity().getContent(), boundary);

            multipartStream.skipPreamble();
            // TODO Dependant on order used by NRS
            // Read JSON
            ByteArrayOutputStream jsonStream = new ByteArrayOutputStream();
            multipartStream.readHeaders();
            multipartStream.readBodyData(jsonStream);
            multipartStream.readBoundary();
            JSONObject jsonObject = parseJson(jsonStream.toString());
            jsonStream.close();
            addContentType(io.getIdentifier(), jsonObject);
            addMetadata(io.getIdentifier(), jsonObject);
            addLocators(io, jsonObject);

            File file = new File(Environment.getExternalStorageDirectory()
            		+ "/DCIM/Shared/" + getHash(io.getIdentifier()));
            FileOutputStream fos = new FileOutputStream(file);
            multipartStream.readHeaders();
            multipartStream.readBodyData(fos);
            fos.flush();
            fos.close();

            // Add file path locator
            Attribute locator = mDatamodelFactory.createAttribute();
            locator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            locator.setIdentification(SailDefinedAttributeIdentification.FILE_PATH.getURI());
            locator.setValue(file.getAbsoluteFile());
            io.addAttribute(locator);

            return io;

        } catch (IOException e) {
            throw new InvalidResponseException("Failed to read InformationObject from response", e);
        }

    }

    /**
     * Create an InformationObject given an identifier and the HTTP response to the NetInf GET.
     * @param identifier
     *     The Identifier used for the NetInf GET
     * @param response
     *     The HTTP response
     * @return
     *     The InformationObject created from the identifier and HTTP response
     * @throws InvalidResponseException
     *     In case the HTTP response doesn't have information needed to create the InformationObject
     */
    private InformationObject handleResponse(Identifier identifier, HttpResponse response)
            throws InvalidResponseException {

        int statusCode = response.getStatusLine().getStatusCode();

        switch (statusCode) {
        case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
            Log.d(TAG, statusCode + ": Response that should contain locators");
            // Just locators
            return readIo(identifier, response);

        case HttpStatus.SC_OK:
            Log.d(TAG, statusCode + ": Response that should contain locators and data");
            return readIoAndFile(identifier, response);

        default:
            Log.w(TAG, statusCode + ": Unexpected Response Code");
            throw new InvalidResponseException("Unexpected Response Code = " + statusCode);
        }
    }

    /**
     * Performs a NetInf GET request using the HTTP convergence layer.
     * @param identifier       Identifier describing the InformationObject to get
     * @return                 The InformationObject resulting from the NetInf GET
     *                         or null if the get failed.
     */
    @Override
    public InformationObject get(Identifier identifier) {
        try {
            // Create NetInf GET request
            String uri = "ni:///" + getHashAlg(identifier) + ";" + getHash(identifier);
            HttpPost getRequest = createGet(uri);

            // Execute NetInf GET request
            HttpResponse response = mClient.execute(getRequest);

            // Handle the response
            InformationObject io = handleResponse(identifier, response);
            return io;

        } catch (InvalidResponseException e) {
            Log.e(TAG, "InvalidResponseException: " + (e.getMessage() != null ? e.getMessage() : ""));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "get() failed, UnsupportedEncodingException, returning null");
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + (e.getMessage() != null ? e.getMessage() : ""));
        }
        Log.e(TAG, "get() failed. Returning null");
        return null;
    }

    @Override
    public List<Identifier> getAllVersions(Identifier arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void put(InformationObject io) {

        try {
            HttpPost post = createPublish(io);
            HttpResponse response = mClient.execute(post);
            Log.d(TAG, "statusCode = "
                    + Integer.toString(response.getStatusLine().getStatusCode()));

        } catch (UnsupportedEncodingException e) {
            throw new NetInfResolutionException("Encoding not supported", e);
        } catch (IOException e) {
            throw new NetInfResolutionException("Unable to connect to NRS", e);
        }
    }

    /**
     * Creates an HTTP POST representation of a NetInf PUBLISH message.
     * @param io
     *     The information object to publish
     * @return
     *     A HttpPost representing the NetInf PUBLISH message
     * @throws UnsupportedEncodingException
     *     In case the encoding is not supported
     */
    private HttpPost createPublish(InformationObject io)
            throws UnsupportedEncodingException {

        // Extracting values from IO's identifier
        String hashAlg      = getHashAlg(io.getIdentifier());
        String hash         = getHash(io.getIdentifier());
        String contentType  = getContentType(io.getIdentifier());
        String meta         = getMetadata(io.getIdentifier());
        String bluetoothMac = getBluetoothMac(io);
        String filePath     = getFilePath(io);

        HttpPost post = new HttpPost(HTTP + getHost() + ":" + getPort() + "/netinfproto/publish");

        MultipartEntity entity = new MultipartEntity();

        StringBody uri = new StringBody("ni:///" + hashAlg + ";" + hash + "?ct=" + contentType);
        entity.addPart("URI", uri);

        StringBody msgid =
                new StringBody(Integer.toString(mRandomGenerator.nextInt(MSG_ID_MAX)));
        entity.addPart("msgid", msgid);

        if (bluetoothMac != null) {
            StringBody l = new StringBody(bluetoothMac);
            entity.addPart("loc1", l);
        }

        if (meta != null) {
            StringBody ext = new StringBody(meta.toString());
            entity.addPart("ext", ext);
        }

        if (filePath != null) {
            StringBody fullPut = new StringBody("true");
            entity.addPart("fullPut", fullPut);
            FileBody octets = new FileBody(new File(filePath));
            entity.addPart("octets", octets);
        }

        StringBody rform = new StringBody("json");
        entity.addPart("rform", rform);

        /* Used to print the message sent to the NRS
        try {
            entity.writeTo(System.out);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write MultipartEntity to System.out");
        } */
        post.setEntity(entity);
        return post;
    }

    /**
     * Creates an HTTP Post request to get an IO from the NRS.
     * @param uri                              the NetInf format URI for getting IOs
     * @return                                 The HTTP Post request
     * @throws UnsupportedEncodingException    In case UTF-8 is not supported
     */
    private HttpPost createGet(String uri) throws UnsupportedEncodingException {

        HttpPost post = new HttpPost(HTTP + getHost() + ":" + getPort() + "/netinfproto/get");

        String msgid = Integer.toString(mRandomGenerator.nextInt(MSG_ID_MAX));
        String ext = "no extension";

        String completeUri = "URI=" + uri + "&msgid=" + msgid  + "&ext=" + ext;

        String encodeUrl = null;

        encodeUrl = URLEncoder.encode(completeUri, "UTF-8");

        HttpEntity newEntity =
                new InputStreamEntity(fromString(encodeUrl), encodeUrl.getBytes().length);

        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(newEntity);

        return post;
    }

    @Override
    protected ResolutionServiceIdentityObject createIdentityObject() {
        ResolutionServiceIdentityObject identity = mDatamodelFactory
                .createDatamodelObject(ResolutionServiceIdentityObject.class);
        identity.setName(TAG);
        int priority = Integer.parseInt(UProperties.INSTANCE
	    		.getPropertyWithName("nrs.priority"));
        identity.setDefaultPriority(priority);
        identity.setDescription(describe());
        return identity;
    }

    /**
     * Converts an InputStream into a String.
     * TODO Move to Util class, probably use the better commented version from NetInfRequest
     * @param input A input stream
     * @return String representation of the input stream
     */
    private String streamToString(InputStream input) {
        try {
            return new Scanner(input).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    /**
     * Converts a string to a type ByteArrayInputStream.
     *
     * @param str string to be converted
     *
     * @return ByteArrayInputStream
     */
    private static InputStream fromString(String str) {
        byte[] bytes = str.getBytes();
        return new ByteArrayInputStream(bytes);
    }
}
