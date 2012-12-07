package project.cs.netinfservice.util;

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
     * @param datamodelFactory
     *       The DatamodelFactory that will be used to create the identifier.
     */
    public IdentifierBuilder(DatamodelFactory datamodelFactory) {
        mDatamodelFactory = datamodelFactory;
        mIdentifier = mDatamodelFactory.createIdentifier();
    }

    public Identifier build() {
        return mIdentifier;
    }

    /**
     * Sets the hash.
     * @param hash
     *      The hash
     * @return
     *      The builder
     */
    public IdentifierBuilder setHash(String hash) {
        return setLabel(SailDefinedLabelName.HASH_CONTENT.getLabelName(), hash);
    }

    /**
     * Sets the hash algorithm.
     * @param hashAlg
     *      The hash algorithm
     * @return
     *      The builder
     */
    public IdentifierBuilder setHashAlg(String hashAlg) {
        return setLabel(SailDefinedLabelName.HASH_ALG.getLabelName(), hashAlg);
    }

    /**
     * Sets the metadata.
     * @param jsonMetadata
     *      The metadata as a JSON string
     * @return
     *      The builder
     */
    public IdentifierBuilder setMetadata(String jsonMetadata) {
        return setLabel(SailDefinedLabelName.META_DATA.getLabelName(), jsonMetadata);
    }

    /**
     * Sets a label.
     * @param labelName
     *      The name of the label
     * @param labelValue
     *      The value to set it to
     * @return
     *      The builder
     */
    private IdentifierBuilder setLabel(String labelName, String labelValue) {
        IdentifierLabel label = mDatamodelFactory.createIdentifierLabel();
        label.setLabelName(labelName);
        label.setLabelValue(labelValue);
        mIdentifier.addIdentifierLabel(label);
        return this;
    }

}
