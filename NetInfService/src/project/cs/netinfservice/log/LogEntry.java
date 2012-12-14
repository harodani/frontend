package project.cs.netinfservice.log;

import java.io.File;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedAttributeIdentification;
import project.cs.netinfservice.netinf.common.datamodel.SailDefinedLabelName;
import project.cs.netinfutilities.UProperties;
import android.os.Environment;


public class LogEntry {
    public static final String TAG = "LogEntry";

    public static final String mSharedFolder =
            UProperties.INSTANCE.getPropertyWithName("sharing.folder");

    public static final String mExternalStorage =
            Environment.getExternalStorageDirectory().getAbsolutePath();

    private Type mType; // NRS, Bluetooth, Database
    private Action mAction; // Sending file, retrieving file
    private long mStartTime;
    private long mStopTime;
    private String mHash;
    private long mTransferredBytes = 0;
    private boolean mFailed = false;

    public enum Type {
        BLUETOOTH,
        NRS,
        DATABASE,
    }

    public enum Action {
        PUBLISH,
        PUBLISH_WITH_FILE,
        GET,
        GET_WITH_FILE,
    }

    public LogEntry(String hash, Type type, Action action) {
        mHash = hash;
        mType = type;
        mAction = action;
        mStartTime = System.currentTimeMillis();
    }

    public LogEntry(Identifier identifier, Type type, Action action) {
        this(getHash(identifier), type, action);
    }

    // TODO disc reading slow? do before getting time?
    public LogEntry(InformationObject io, Type type, Action action) {
        this(io.getIdentifier(), type, action);
        if (action == Action.PUBLISH && getFilePath(io) != null) {
            action = Action.PUBLISH_WITH_FILE;
            calculateTransferredSize();
        }
    }

    public void stop() {
        mStopTime = System.currentTimeMillis();
        NetInfLog.writeLog();
    }

    public void stop(InformationObject io) {
        if (getFilePath(io) != null) {
            mAction = Action.GET_WITH_FILE;
            calculateTransferredSize();
        }
        stop();
    }

    public void stop(int bytes) {
        mTransferredBytes = bytes;
        stop();
    }

    public void failed() {
        mFailed = true;
        stop();
    }

    private void calculateTransferredSize() {
//        mTransferredBytes = transferredBytes;
        File file = new File(mExternalStorage + mSharedFolder + mHash);
        if (file.exists()) {
            mTransferredBytes = file.length();
        } else {
            mTransferredBytes = -1;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(mType);
        builder.append("\t");
        builder.append(mAction);
        builder.append("\t");
        builder.append(mStopTime - mStartTime);
        if (mTransferredBytes != 0) {
            builder.append("\t");
            builder.append(mTransferredBytes);
        }
        if (mFailed) {
            builder.append("\t");
            builder.append("FAILED");
        }
        return builder.toString();
    }

    private static String getHash(Identifier identifier) {
        String hash = identifier.getIdentifierLabel(
                SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
        return hash;
    }

    private static String getFilePath(InformationObject io) {
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

}
