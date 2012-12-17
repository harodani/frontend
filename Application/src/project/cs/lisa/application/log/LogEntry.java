package project.cs.lisa.application.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

import project.cs.netinfutilities.UProperties;
import android.os.Environment;
import android.util.Log;

/**
 * Represents an entry in the log file.
 * @author Linus Sunde
 * @author Thiago Costa Porto
 */
public class LogEntry {

    /** Log Tag. */
    public static final String TAG = "LogEntry";

    /** Log File. */
    public static final String LOG_FILE =
            UProperties.INSTANCE.getPropertyWithName("log.file");

    /** External Storage. */
    public static final String EXTERNAL_STORAGE =
            Environment.getExternalStorageDirectory().getAbsolutePath();

    /** Timestamp Format. */
    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /** Timestamp. */
    private String mTimestamp;

    /** Type. */
    private Type mType;

    /** Action. */
    private Action mAction;

    /** Start. */
    private long mStartTime;

    /** Stop. */
    private long mStopTime;

    /** Data transfered. */
    private long mTransferredBytes = 0;

    /** Failed flag. */
    private boolean mFailed = false;

    /** Different types of entries. */
    public enum Type {
        /** Uplink, usually Internet. */
        UPLINK;
    }

    /** Different types of actions. */
    public enum Action {
        /** Get request where response contained a file. */
        GET_WITH_FILE;
    }

    /**
     * Creates a new LogEntry.
     * @param type
     *      The type of the LogEntry.
     * @param action
     *      The action of the LogEntry.
     */
    public LogEntry(final Type type, final Action action) {
        mTimestamp = DATE_FORMAT.format(Calendar.getInstance().getTime());
        mType = type;
        mAction = action;
        mStartTime = System.currentTimeMillis();
    }

    /**
     * Writes the LogEntry to the log file.
     */
    private void writeToFile() {
        try {
            FileUtils.write(
                    new File(EXTERNAL_STORAGE + LOG_FILE),
                    toString() + "\n",
                    true);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write log entry to file");
        }
    }

    /**
     * Signals that the action is done and logs the entry to file.
     */
    public void done() {
        mStopTime = System.currentTimeMillis();
        writeToFile();
    }

    /**
     * Signals that the action is done and logs the entry to file.
     * @param bytes
     *      The bytes downloaded by the action
     */
    public void done(final byte[] bytes) {
        mTransferredBytes = bytes.length;
        done();
    }

    /**
     * Signals that the action failed and logs the entry to file.
     */
    public void failed() {
        mFailed = true;
        done();
    }

    /**
     * Create a string representation of a LogEntry.
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(mTimestamp);
        builder.append("\t");
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

    /**
     * Deletes the log file.
     * @return
     *      true if the log file was deleted, otherwise false
     */
    public static boolean deleteLogFile() {
        return FileUtils.deleteQuietly(new File(EXTERNAL_STORAGE + LOG_FILE));
    }

}
