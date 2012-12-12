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
 */package project.cs.netinfservice.util;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;

/**
 * A Builder that makes it easier to create identifiers.
 * @author Linus Sunde
 */
public class IdentifierBuilder {
    /** DatamodelFactory used to create the identifier. */
    private DatamodelFactory mDatamodelFactory;
    
    /** The identifier under construction. */
    private Identifier mIdentifier;

    /**
     * Creates a new builder.
     * 
     * @param datamodelFactory
     *      The <i>DatamodelFactory</i> that will be used to create the identifier.
     */
    public IdentifierBuilder(DatamodelFactory datamodelFactory) {
        // Set up private DataModel Factory
        mDatamodelFactory = datamodelFactory;
        
        // Create a new identifier for this object
        mIdentifier = mDatamodelFactory.createIdentifier();
    }

    /**
     * Returns the built Identifier.
     * 
     * @return
     *      The identifier built by running this class.
     */
    public Identifier build() {
        // Identifier built by the class.
        return mIdentifier;
    }

    /**
     * Sets the hash.
     * 
     * @param hash
     *      The hash
     * @return
     *      The builder
     */
    public IdentifierBuilder setHash(String hash) {
        // Set the hash in the identifier being built
        return setLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName(), hash);
    }

    /**
     * Sets the hash algorithm.
     * 
     * @param hashAlg
     *      The hash algorithm
     * @return
     *      The builder
     */
    public IdentifierBuilder setHashAlg(String hashAlg) {
        // Sets the hash algorithm used to hash the object in the identifier being built 
        return setLabel(SailDefinedLabelName.HASH_ALG.getLabelName(), hashAlg);
    }

    /**
     * Sets the Metadata.
     * 
     * @param jsonMetadata
     *      The Metadata as a JSON string
     * @return
     *      The builder
     */
    public IdentifierBuilder setMetadata(String jsonMetadata) {
        // Sets the metadata
        return setLabel(SailDefinedLabelName.META_DATA.getLabelName(), jsonMetadata);
    }

    /**
     * Sets a label.
     * 
     * @param labelName
     *      The name of the label
     * @param labelValue
     *      The value to set it to
     * @return
     *      The builder
     */
    private IdentifierBuilder setLabel(String labelName, String labelValue) {
        // Create an identifier label and add the value to it
        IdentifierLabel label = mDatamodelFactory.createIdentifierLabel();
        label.setLabelName(labelName);
        label.setLabelValue(labelValue);
        
        // Add label to identifier
        mIdentifier.addIdentifierLabel(label);
        
        // Returns this builder
        return this;
    }
}
