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
package project.cs.netinfservice.util;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;

import org.json.simple.JSONArray;

import project.cs.netinfservice.netinf.common.datamodel.SailDefinedAttributeIdentification;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.netinfutilities.UProperties;
import project.cs.netinfutilities.metadata.Metadata;

/**
 * A Builder that makes it easier to create information objects.
 *
 * @author Kim-Anh Tran
 */
public class IOBuilder {
    /** All bluetooth locators have the following indicator in their address. */
    public static final String BLUETOOTH_LOCATOR_PREFIX = "nimacbt://";

    /** The label for identifying content types. */
    public static final String CONTENT_TYPE_LABEL =
            SailDefinedLabelName.CONTENT_TYPE.getLabelName();

    /** The label for identifying the hash contents. */
    public static final String HASH_LABEL =
            SailDefinedLabelName.HASH_CONTENT.getLabelName();

    /** The label for identifying the hash algorithm. */
    public static final String HASH_ALG_LABEL = SailDefinedLabelName.HASH_ALG.getLabelName();

    /** The label for identifying the meta data. */
    public static final String META_LABEL = SailDefinedLabelName.META_DATA.getLabelName();

    /** The metadata label for urls. */
    public static final String URL_LABEL =
            UProperties.INSTANCE.getPropertyWithName("metadata.url");

    /** The datamodel factory that is needed to create information objects. */
    private DatamodelFactory mFactory;

    /** The identifier of the information object we are creating. */
    private Identifier mIdentifier;

    /** The Metadata. */
    private Metadata mMetadata;

    /** The Information Object. */
    private InformationObject mIo;

    /** The url array that contains all urls corresponding to this IO. */
    private JSONArray mUrlArray;

    /**
     * Creates a new Builder.
     *
     * @param factory
     *     The factory for creating the information object
     */
    public IOBuilder(DatamodelFactory factory) {
        // Datamodel Factory
        mFactory = factory;

        // Create the new Identifier
        mIdentifier = mFactory.createIdentifier();

        // Metadata helper object
        mMetadata = new Metadata();

        // New Information Object
        mIo = mFactory.createInformationObject();

        // Create the JSON formatted URL Array
        mUrlArray = new JSONArray();
    }

    /**
     * Creates a new Builder with the given meta data.
     * 
     * @param factory
     * 		The datamodel factory
     * @param jsonMetadata
     *     	The metadata string encoded in json
     */
    public IOBuilder(DatamodelFactory factory, String jsonMetadata) {
        // Builds new factory factory
        this(factory);

        // Set the metadata to the jsonMetadata passed as parameter
        mMetadata = new Metadata(jsonMetadata);
    }

    /**
     * Sets the hash value.
     *
     * @param hash
     *     	The hash value of the information object.
     * @return
     * 		Returns this Builder.
     */
    public IOBuilder setHash(String hash) {
        // Adds hash to identifier
        addIdentifierLabel(mIdentifier, HASH_LABEL, hash);

        // Returns self
        return this;
    }

    /**
     * Sets the hash algorithm.
     *
     * @param hashAlgorithm
     *     	The hash algorithm of the information object.
     * @return
     * 		Returns this Builder.

     */
    public IOBuilder setHashAlgorithm(String hashAlgorithm) {
        // Adds the hash algorithm to private identifier
        addIdentifierLabel(mIdentifier, HASH_ALG_LABEL, hashAlgorithm);

        // Returns self
        return this;
    }

    /**
     * Sets the content type.
     *
     * @param contentType
     *     	The content type of the information object.
     * @return
     *     	Returns this Builder.

     */
    public IOBuilder setContentType(String contentType) {
        // Adds content-type to identifier
        addIdentifierLabel(mIdentifier, CONTENT_TYPE_LABEL, contentType);

        // Returns self
        return this;
    }

    /**
     * Adds a metadata key value pair to the information object.
     *
     * @param key
     *     	The key of the metadata
     * @param value
     *     	The value of the metadata
     * @return
     * 		Returns this Builder.
     */
    @SuppressWarnings("unchecked") // mUrlArray.add(value)
    public IOBuilder addMetaData(String key, String value) {
        // Check if we are inserting inside URL
        if (key.equals(URL_LABEL)) {
            mUrlArray.add(value);
        } else {
            // If not, just add the metadata
            mMetadata.insert(key, value);			
        }

        // Returns self
        return this;
    }

    /**
     * Sets the metadata (overwriting any previously added) of the information object.
     * 
     * @param jsonMetadata
     *     the metadata as a json string
     * @return
     *     the builder
     */
    public IOBuilder setMetaData(String jsonMetadata) {
        // OVERWRITES metadata
        mMetadata = new Metadata(jsonMetadata);

        // Returns self
        return this;
    }

    /**
     * Adds a bluetooth locator to the information object.
     * 
     * @param bluetoothMac
     *     The bluetooth MAC address
     * @return
     *     The builder
     */
    public IOBuilder addBluetoothLocator(String bluetoothMac) {
        // Adds an attribute to the Information Object
        addAttribute(
                DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString(),
                SailDefinedAttributeIdentification.BLUETOOTH_MAC.getURI(),
                BLUETOOTH_LOCATOR_PREFIX + bluetoothMac);

        // Returns self
        return this;
    }

    /**
     * Adds a file path locator to the information object.
     * 
     * @param bluetoothMac
     *     The file path locator
     * @return
     *     The builder
     */
    public IOBuilder addFilePathLocator(String bluetoothMac) {
        // Adds filepath locator to the Information Object
        addAttribute(
                DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString(),
                SailDefinedAttributeIdentification.FILE_PATH.getURI(),
                bluetoothMac);

        // Returns self
        return this;
    }

    /**
     * Creates an information object based on the Builder object.
     * This is the function that puts the InformationObject all together.
     *
     * @return
     *     	The information object that was created.
     */
    public InformationObject build() {
        // Adds URL Array to Metadata
        mMetadata.insert(URL_LABEL, mUrlArray);

        String metaString;

        // Convert metadata to string
        if (mMetadata.convertToString().contains("meta")) {
            // The metadata was 'setted' and not created from scratch
            metaString = mMetadata.convertToString();
            // TODO: Log function?
            System.out.println("Metadata string: " + metaString);
        } else {
            // Creates a JSON string with the key "meta" set to the JSON string of the metadata.
            metaString = mMetadata.convertToMetadataString();
        }

        // Add metadata to Identifier
        addIdentifierLabel(mIdentifier, META_LABEL, metaString);

        // Set identifier
        mIo.setIdentifier(mIdentifier);

        // Returns Information Object
        return mIo;
    }

    /**
     * Adds an identifier label for the specified identifier for the passed label properties.
     *
     * @param identifier
     *     	The identifier to modify
     * @param labelName
     * 		The label name
     * @param labelValue
     *     	The label value
     */
    private void addIdentifierLabel(Identifier identifier, String labelName, String labelValue) {
        // Creates new identifier label
        IdentifierLabel identifierLabel = mFactory.createIdentifierLabel();
        
        // Set label to have name 'labelName' and value 'labelValue'
        identifierLabel.setLabelName(labelName);
        identifierLabel.setLabelValue(labelValue);
        
        // Add new created label to the identifier
        identifier.addIdentifierLabel(identifierLabel);
    }

    /**
     * Adds an an Attribute to the current information object.
     * 
     * @param attributePurpose
     * 		The purpose of the attribute
     * @param attributeIdentification
     *  	The attribute identification 
     * @param attributeValue
     * 		The attribute value
     */
    private void addAttribute(String attributePurpose, String attributeIdentification,
            String attributeValue) {
        // Create a new attribute
        Attribute attribute = mFactory.createAttribute();
        
        // Set attribute purpose
        attribute.setAttributePurpose(attributePurpose);
        
        // Set attributes ID (name)
        attribute.setIdentification(attributeIdentification);
        
        // Set attributes value
        attribute.setValue(attributeValue);
        
        // Add attribute to Information Object
        mIo.addAttribute(attribute);
    }
}
