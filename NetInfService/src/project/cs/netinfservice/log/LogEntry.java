package project.cs.netinfservice.log;

import java.io.File;
import java.io.IOException;

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;

import org.apache.commons.io.FileUtils;

import project.cs.netinfservice.netinf.common.datamodel.SailDefinedAttributeIdentification;
import project.cs.netinfutilities.UProperties;
import android.os.Environment;
import android.util.Log;

public class LogEntry {
    public static final String TAG = "LogEntry";

    public static final String LOG_FILE =
            UProperties.INSTANCE.getPropertyWithName("log.file");
    public static final String EXTERNAL_STORAGE =
            Environment.getExternalStorageDirectory().getAbsolutePath();

    private Type mType; // NRS, Bluetooth, Database
    private Action mAction; // Sending file, retrieving file
    private long mStartTime;
    private long mStopTime;
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

    public LogEntry(Type type, Action action) {
        mType = type;
        mAction = action;
        mStartTime = System.currentTimeMillis();
    }

    // TODO disc reading slow? do before getting time?
    public LogEntry(InformationObject io, Type type, Action action) {
        this(type, action);
        if (action == Action.PUBLISH && getFilePath(io) != null) {
            action = Action.PUBLISH_WITH_FILE;
            mTransferredBytes = calculateTransferredBytes(io);
        }
    }

    private long calculateTransferredBytes(InformationObject io) {
        File file = new File(getFilePath(io));
        if (file.exists()) {
            return file.length();
        }
        return -1;
    }

    private void writeToFile() {
        try {
            FileUtils.write(new File(EXTERNAL_STORAGE + LOG_FILE), toString(), true);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write log entry to file");
        }
    }

    public void stop() {
        mStopTime = System.currentTimeMillis();
        writeToFile();
    }

    public void stop(InformationObject io) {
        if (getFilePath(io) != null) {
            mAction = Action.GET_WITH_FILE;
            mTransferredBytes = calculateTransferredBytes(io);
        }
        stop();
    }

    public void stop(byte[] bytes) {
        mTransferredBytes = bytes.length;
        stop();
    }

    public void failed() {
        mFailed = true;
        stop();
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

//    private static String getHash(Identifier identifier) {
//        String hash = identifier.getIdentifierLabel(
//                SailDefinedLabelName.HASH_CONTENT.getLabelName()).getLabelValue();
//        return hash;
//    }

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

    public static boolean deleteLogFile() {
        return FileUtils.deleteQuietly(new File(EXTERNAL_STORAGE + LOG_FILE));
    }

}
