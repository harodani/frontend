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
import project.cs.netinfservice.log.LogEntry;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedAttributeIdentification;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.netinfservice.netinf.node.exceptions.InvalidResponseException;
import project.cs.netinfutilities.UProperties;
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
 */
public class NameResolutionService extends AbstractResolutionServiceWithoutId
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
     * @param host
     *      The NRS IP Address
     * @param port
     *      The NRS Port
     * @param datamodelFactory
     *      Creates different objects necessary in the NetInf model
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

        // Setup other private variables
        mHost = host;
        mPort = port;
        mDatamodelFactory = datamodelFactory;
    }

    /**
     * Get the NRS Address.
     *
     * @return
     *      <p>The IP Address of the NRS.
     */
    private String getHost() {
        // Shared preferences
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(MainNetInfActivity.getActivity());

        // Host is set to NRS IP that is set in the shared preferences. If none is set, uses
        // default value stored in mHost (set by constructor)
        mHost = sharedPreferences.getString(PREF_KEY_NRS_IP, mHost);

        // Returns NRS IP.
        return mHost;
    }

    /**
     * Get the NRS port.
     *
     * @return
     *      <p>The port of the NRS
     */
    private int getPort() {
        // Shared preferences
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(MainNetInfActivity.getActivity());

        // Port is set to the NRS PORT that is set in the shared preferences. If none is set,
        // uses the default value stored in mPort (set by constructor)
        mPort = Integer.parseInt(sharedPreferences
                .getString(PREF_KEY_NRS_PORT, Integer.toString(mPort)));

        // Returns NRS PORT
        return mPort;
    }

    /**
     * Gets the hash algorithm from an identifier.
     *
     * @param identifier
     *      The identifier
     * @return
     *      The hash algorithm
     */
    private String getHashAlg(Identifier identifier) {
        // Extract hash algorithm
        String hashAlg = identifier.getIdentifierLabel(
                SailDefinedLabelName.HASH_ALG.getLabelName()).getLabelValue();

        // Return the hash algorithm used to hash the object
        return hashAlg;
    }

    /**
     * Gets the hash from an identifier.
     *
     * @param identifier
     *      The identifier
     * @return
     *      The hash
     */
    private String getHash(Identifier identifier) {
        // Extracts hash
        String hash = identifier.getIdentifierLabel(
                SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();

        // Returns object's hash
        return hash;
    }

    /**
     * Gets the content-type from an identifier.
     *
     * @param identifier
     *      The identifier
     * @return
     *      The content-type
     */
    private String getContentType(Identifier identifier) {
        // Extracts content-type
        String contentType = identifier.getIdentifierLabel(
                SailDefinedLabelName.CONTENT_TYPE.getLabelName()).getLabelValue();

        // Returns MIME content-type
        return contentType;
    }

    /**
     * Gets the metadata from an identifier.
     *
     * @param identifier
     *      The identifier
     * @return
     *      The metadata
     */
    private String getMetadata(Identifier identifier) {
        // Extracts metadata
        String metadata = identifier.getIdentifierLabel(
                SailDefinedLabelName.META_DATA.getLabelName()).getLabelValue();

        // Returns extracted metadata (in STRING form)
        return metadata;
    }

    /**
     * Gets the file path from an InformationObject.
     *
     * @param io
     *      The information object
     * @return
     *      The file path
     */
    private String getFilePath(InformationObject io) {
        // Created a new attribute (as defined on datamodel factory)
        Attribute filepathAttribute =
                io.getSingleAttribute(SailDefinedAttributeIdentification.FILE_PATH.getURI());

        // Extract filepath
        String filepath = null;
        if (filepathAttribute != null) {
            filepath = filepathAttribute.getValueRaw();
            filepath = filepath.substring(filepath.indexOf(":") + 1);
        }

        // Returns file path
        return filepath;
    }

    /**
     * Gets the <b>first</b> bluetooth locator from an InformationObject.
     *
     * @param io
     *      The information object
     * @return
     *      The bluetooth locator
     */
    private String getBluetoothMac(InformationObject io) {
        // Creates a new attribute to extract information from IO
        Attribute bluetoothAttribute =
                io.getSingleAttribute(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());

        // Extract bluetooth locator from IO
        String bluetoothLocator = null;

        if (bluetoothAttribute != null) {
            bluetoothLocator = bluetoothAttribute.getValueRaw();
            bluetoothLocator = bluetoothLocator.substring(bluetoothLocator.indexOf(":") + 1);
        }

        // Returns FIRST bluetooth locator
        return bluetoothLocator;
    }

    /**
     * Reads the next content stream from a HTTP response, expecting it to be JSON.
     *
     * @param response
     *      The HTTP response
     * @return
     *      The read JSON
     * @throws InvalidResponseException
     *      In case reading the JSON failed
     */
    private String readJson(HttpResponse response) throws InvalidResponseException {
        // Sanity check of the HTTP response received
        if (response == null) {
            // null response
            throw new InvalidResponseException("Response is null.");
        } else if (response.getEntity() == null) {
            // No entity attached with the HTTP response
            throw new InvalidResponseException("Entity is null.");
        } else if (response.getEntity().getContentType() == null) {
            // No content-type with the response
            throw new InvalidResponseException("Content-Type is null.");
        } else if (!response.getEntity().getContentType().getValue().equals("application/json")) {
            // We did not receive a JSON back
            throw new InvalidResponseException("Content-Type is "
                    + response.getEntity().getContentType().getValue()
                    + ", expected \"application/json\"");
        }

        // Try to convert HTTP response to a JSON String
        try {
            String jsonString = streamToString(response.getEntity().getContent());
            return jsonString;
        } catch (IOException e)  {
            throw new InvalidResponseException("Failed to convert stream to string.", e);
        }
    }

    /**
     * Converts the JSON String returned in the HTTP response into a JSONObject.
     *
     * @param jsonString
     *      The JSON String from the HTTP response
     * @return
     *      The JSONObject
     * @throws InvalidResponseException
     *      In case the JSON String is invalid
     */
    private JSONObject parseJson(String jsonString) throws InvalidResponseException {
        // Creates a JSON Object from the JSON String passed as parameter
        JSONObject json = (JSONObject) JSONValue.parse(jsonString);

        // If we were unable to get a JSON Object from the JSON String, throw exception.
        if (json == null) {
            Log.e(TAG, "Unable to parse JSON");
            throw new InvalidResponseException("Unable to parse JSON.");
        }

        // Return JSON Object
        return json;
    }

    /**
     * If the JSON contains a content-type, extract it and set the content-type of the identifier.
     *
     * @param identifier
     *      The identifier
     * @param json
     *      The JSON
     */
    private void addContentType(Identifier identifier, JSONObject json) {
        // Check that the content-type is a string
        Object object = json.get("ct");

        if (!(object instanceof String)) {
            Log.w(TAG, "Content-Type NOT added.");
            return;
        }

        // Converts JSON value to a string
        String contentType = (String) object;

        // Create a new identifier label and get the content-type
        IdentifierLabel label = mDatamodelFactory.createIdentifierLabel();
        label.setLabelName(SailDefinedLabelName.CONTENT_TYPE.getLabelName());
        label.setLabelValue(contentType);

        // Attach it to the identifier
        identifier.addIdentifierLabel(label);
    }

    /**
     * If the JSON contains metadata, extract it and set the metadata of the identifier.
     *
     * @param identifier
     *      The identifier
     * @param json
     *      The JSON
     */
    private void addMetadata(Identifier identifier, JSONObject json) {
        // Check that the metadata is an JSONObject
        Object object = json.get("metadata");

        if (!(object instanceof JSONObject)) {
            Log.w(TAG, "Metadata NOT added.");
        }

        // Extract metadata from JSON Value
        JSONObject metadata = (JSONObject) object;

        // Create identifierlabel and add the metadata to it
        IdentifierLabel label = mDatamodelFactory.createIdentifierLabel();
        label.setLabelName(SailDefinedLabelName.META_DATA.getLabelName());
        label.setLabelValue(metadata.toJSONString());

        // Attach metadata to identifier
        identifier.addIdentifierLabel(label);
    }

    /**
     * If the JSON contains locators, extract them and add them to the InformationObject.
     *
     * @param io
     *      The InformationObject
     * @param json
     *      The JSON
     */
    private void addLocators(InformationObject io, JSONObject json) {
        // Get locators from JSON object
        JSONArray locators = (JSONArray) json.get("loc");

        // Iterate through locators list and add them to the IO
        for (Object locator : locators) {
            // Create a new attribute and add the locator to it
            Attribute newLocator = mDatamodelFactory.createAttribute();
            newLocator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            newLocator.setIdentification(SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI());
            newLocator.setValue(locator);

            // Attach locator to the IO
            io.addAttribute(newLocator);
        }
    }

    /**
     * Create an IO based in an identifier and a HTTP Response.
     *
     * @param identifier
     *      The IO identifier
     * @param response
     *      The HTTP Response
     * @return
     *      A new IO
     * @throws InvalidResponseException
     *      If the response is not correct
     */
    private InformationObject readIo(Identifier identifier, HttpResponse response)
            throws InvalidResponseException {
        // Creates a new InformationObject
        InformationObject io = mDatamodelFactory.createInformationObject();

        // Set the IO identifier to the one passed with the function
        io.setIdentifier(identifier);

        // Reads JSON Object
        String jsonString = readJson(response);

        // Parses JSON Object
        JSONObject json = parseJson(jsonString);

        // Add content-type, metadata and locators
        addContentType(identifier, json);
        addMetadata(identifier, json);
        addLocators(io, json);

        // Returns new Information Object with content-type, metadata and locators.
        return io;
    }

    /**
     * Creates an Information Object and file from a previous HTTP request.
     *
     * @param identifier
     *      The identifier
     * @param response
     *      HTTP response
     * @return
     *      New Information Object created from the response.
     *      <p>A side-effect is the new file created.</p>
     * @throws InvalidResponseException
     *      Thrown if the response had an invalid information object.
     */
    private InformationObject readIoAndFile(Identifier identifier,
            HttpResponse response) throws InvalidResponseException {
        // Sanity checks the response.
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

        // Reads IO and File
        try {
            // Create IO
            InformationObject io = mDatamodelFactory.createInformationObject();
            io.setIdentifier(identifier);

            // Extract Content-type from header
            String contentType = response.getHeaders("Content-Type")[0].getValue();

            // Get boundary bytes
            byte[] boundary = (contentType.substring(contentType.indexOf("boundary=") + 9)).getBytes();

            // Raises intent
            Intent intent = new Intent(NRS_TRANSMISSION);
            MainNetInfActivity.getActivity().sendBroadcast(intent);

            // Start multipart
            @SuppressWarnings("deprecation")
            MultipartStream multipartStream =
            new MultipartStream(response.getEntity().getContent(), boundary);

            // Skip multipart preamble
            multipartStream.skipPreamble();

            // TODO Dependant on order used by NRS
            // Read JSON
            ByteArrayOutputStream jsonStream = new ByteArrayOutputStream();

            // Move on multipart stream
            multipartStream.readHeaders();
            multipartStream.readBodyData(jsonStream);
            multipartStream.readBoundary();

            // Parse JSON Object
            JSONObject jsonObject = parseJson(jsonStream.toString());

            // Close stream used to read JSON
            jsonStream.close();

            // Add attributes to the new Information Object
            addContentType(io.getIdentifier(), jsonObject);
            addMetadata(io.getIdentifier(), jsonObject);
            addLocators(io, jsonObject);

            // Create the new file
            File file = new File(Environment.getExternalStorageDirectory()
                    + "/DCIM/Shared/" + getHash(io.getIdentifier()));

            // Write file in disk
            FileOutputStream fos = new FileOutputStream(file);

            // move on Multipart
            multipartStream.readHeaders();
            multipartStream.readBodyData(fos);

            // Close file stream
            fos.flush();
            fos.close();

            // Add file path locator
            Attribute locator = mDatamodelFactory.createAttribute();
            locator.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            locator.setIdentification(SailDefinedAttributeIdentification.FILE_PATH.getURI());
            locator.setValue(file.getAbsoluteFile());

            // Add atributes
            io.addAttribute(locator);

            // Return new Information Object created
            return io;
        } catch (IOException e) {
            throw new InvalidResponseException("Failed to read InformationObject from response", e);
        }
    }

    /**
     * Create an InformationObject given an identifier and the HTTP response to the NetInf GET.
     *
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
        // Get status code from response
        int statusCode = response.getStatusLine().getStatusCode();

        // Go through possible responses
        switch (statusCode) {
            // HTTP 203
            case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
                Log.d(TAG, statusCode + ": Response that should contain locators");
                // Just locators
                return readIo(identifier, response);

            // HTTP 200
            case HttpStatus.SC_OK:
                Log.d(TAG, statusCode + ": Response that should contain locators and data");
                // Read IO and the File
                return readIoAndFile(identifier, response);

            // Everything else
            default:
                Log.w(TAG, statusCode + ": Unexpected Response Code");
                throw new InvalidResponseException("Unexpected Response Code = " + statusCode);
        }
    }

    /**
     * Performs a NetInf GET request using the HTTP convergence layer.
     *
     * @param identifier
     *      Identifier describing the InformationObject to get
     * @return
     *      The InformationObject resulting from the NetInf GET or null if the get failed.
     *      <p><b>null</b> if get() fails.
     */
    @Override
    public InformationObject get(Identifier identifier) {
        Log.d(TAG, "Get information object from NRS.");

        LogEntry logEntry = new LogEntry(LogEntry.Type.NRS, LogEntry.Action.GET);

        try {
            // Create NetInf GET request. Request looks like ni:///hash-alg;hash
            String uri = "ni:///" + getHashAlg(identifier) + ";" + getHash(identifier);
            HttpPost getRequest = createGet(uri);

            // Execute NetInf GET request
            HttpResponse response = mClient.execute(getRequest);

            // Handle the response
            InformationObject io = handleResponse(identifier, response);

            logEntry.done(io);

            // Returns Information Object found
            return io;
        } catch (InvalidResponseException e) {
            Log.e(TAG, "InvalidResponseException: " + (e.getMessage() != null ? e.getMessage() : ""));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "get() failed, UnsupportedEncodingException, returning null");
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + (e.getMessage() != null ? e.getMessage() : ""));
        }

        logEntry.failed();
        Log.e(TAG, "get() failed. Returning null");

        // Fails
        return null;
    }

    /**
     * Not supported.
     */
    @Override
    public List<Identifier> getAllVersions(Identifier arg0) {
        return null;
    }

    /**
     * Publishes an object to the NRS.
     *
     * @param io
     *      The Information Object to be published
     */
    @Override
    public void put(InformationObject io) {
        // Try to publish to the NRS
        try {
            // Create a new HTTP Post to publish
            HttpPost post = createPublish(io);

            LogEntry logEntry = new LogEntry(io, LogEntry.Type.NRS, LogEntry.Action.PUBLISH);

            // Execute HTTP request
            HttpResponse response = mClient.execute(post);

            // Get status code
            int status = response.getStatusLine().getStatusCode();

            // Check if object was created
            if (status != HttpStatus.SC_CREATED) {
                Log.e(TAG, "Publish to NRS failed, status code: " + status);
                logEntry.failed();
            } else {
                logEntry.done();
            }
        } catch (UnsupportedEncodingException e) {
            throw new NetInfResolutionException("Encoding not supported", e);
        } catch (IOException e) {
            throw new NetInfResolutionException("Unable to connect to NRS", e);
        }
    }

    /**
     * Creates an HTTP POST representation of a NetInf PUBLISH message.
     *
     * @param io
     *     The information object to publish
     * @return
     *     A HttpPost representing the NetInf PUBLISH message
     * @throws UnsupportedEncodingException
     *     In case the encoding is not supported
     */
    private HttpPost createPublish(InformationObject io)
            throws UnsupportedEncodingException {
        // Extracting attributes from IO's identifier
        String hashAlg = getHashAlg(io.getIdentifier());
        String hash = getHash(io.getIdentifier());
        String contentType = getContentType(io.getIdentifier());
        String meta = getMetadata(io.getIdentifier());
        String bluetoothMac = getBluetoothMac(io);
        String filePath = getFilePath(io);

        // Creates a new post. Looks like http://host:port/netinfproto/publish
        HttpPost post = new HttpPost(HTTP + getHost() + ":" + getPort() + "/netinfproto/publish");

        // Create a multipart entity
        MultipartEntity entity = new MultipartEntity();

        // ni:///hashAlgorithm;hash?ct=CONTENT_TYPE
        StringBody uri = new StringBody("ni:///" + hashAlg + ";" + hash + "?ct=" + contentType);
        entity.addPart("URI", uri);

        // Create a new message id
        StringBody msgid =
                new StringBody(Integer.toString(mRandomGenerator.nextInt(MSG_ID_MAX)));
        entity.addPart("msgid", msgid);

        // Add a locator
        if (bluetoothMac != null) {
            StringBody l = new StringBody(bluetoothMac);
            entity.addPart("loc1", l);
        }

        // Add metadata (ext)
        if (meta != null) {
            StringBody ext = new StringBody(meta.toString());
            entity.addPart("ext", ext);
        }

        // Add fullput and octets
        if (filePath != null) {
            StringBody fullPut = new StringBody("true");
            entity.addPart("fullPut", fullPut);
            FileBody octets = new FileBody(new File(filePath));
            entity.addPart("octets", octets);
        }

        // Add form type
        StringBody rform = new StringBody("json");
        entity.addPart("rform", rform);

        //        // Used to print the message sent to the NRS
        //        try {
        //            entity.writeTo(System.out);
        //        } catch (IOException e) {
        //            Log.e(TAG, "Failed to write MultipartEntity to System.out");
        //        }

        // Set attributes to HTTP Post object
        post.setEntity(entity);

        // Return HTTP Post object with all attributes
        return post;
    }

    /**
     * Creates an HTTP Post request to get an IO from the NRS.
     *
     * @param uri
     *      The NetInf format URI for getting IOs
     * @return
     *      The HTTP Post request
     * @throws UnsupportedEncodingException
     *      In case UTF-8 is not supported
     */
    private HttpPost createGet(String uri) throws UnsupportedEncodingException {
        // Create a new post, with url = http://host:port/netinfproto/get
        HttpPost post = new HttpPost(HTTP + getHost() + ":" + getPort() + "/netinfproto/get");

        // Get message id and ext
        String msgid = Integer.toString(mRandomGenerator.nextInt(MSG_ID_MAX));
        String ext = "no extension";

        // Finishes the URI
        String completeUri = "URI=" + uri + "&msgid=" + msgid  + "&ext=" + ext;

        // TODO: Check if this is really necessary.
        // Encodes the URL
        String encodeUrl = null;

        encodeUrl = URLEncoder.encode(completeUri, "UTF-8");

        // New HTTP entity
        HttpEntity newEntity =
                new InputStreamEntity(fromString(encodeUrl), encodeUrl.getBytes().length);

        // Add header to the HTTP Entity
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(newEntity);

        // Return HTTP post created
        return post;
    }

    /**
     * Identity Object creator for this resolution service.
     *
     * @return
     *      The Identity Object for this class.
     */
    @Override
    protected ResolutionServiceIdentityObject createIdentityObject() {
        // Creates identity object
        ResolutionServiceIdentityObject identity = mDatamodelFactory
                .createDatamodelObject(ResolutionServiceIdentityObject.class);

        // Sets the attributes
        identity.setName(TAG);
        int priority = Integer.parseInt(UProperties.INSTANCE
                .getPropertyWithName("nrs.priority"));
        identity.setDefaultPriority(priority);
        identity.setDescription(describe());

        // Returns the new identity object.
        return identity;
    }

    /**
     * Converts an InputStream into a String.
     * TODO Move to Util class, probably use the better commented version from NetInfRequest
     *
     * @param input
     *      A input stream
     * @return String
     *      Rrepresentation of the input stream
     */
    private String streamToString(InputStream input) {
        try {
            return new Scanner(input).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            Log.e(TAG, "NoSuchElementException found. String returned is \"\"");
            return "";
        }
    }

    /**
     * Converts a string to a type ByteArrayInputStream.
     *
     * @param str
     *      String to be converted
     *
     * @return
     *      A new ByteArrayInputStream with the bytes from the string
     */
    private static InputStream fromString(String str) {
        byte[] bytes = str.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Not supported.
     */
    @Override
    public void delete(Identifier arg0) {
    }

    @Override
    public String describe() {
        return "backend NRS";
    }

}
