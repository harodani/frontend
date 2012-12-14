package project.cs.lisa.application.log;

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
        UPLINK;
    }

    public enum Action {
        GET_WITH_FILE;
    }

    public LogEntry(Type type, Action action) {
        mType = type;
        mAction = action;
        mStartTime = System.currentTimeMillis();
    }

    public void stop() {
        mStopTime = System.currentTimeMillis();
        ApplicationLog.writeLog();
    }

    public void stop(String hash, byte[] fileData) {
        mHash = hash;
        mTransferredBytes = fileData.length;
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

}
