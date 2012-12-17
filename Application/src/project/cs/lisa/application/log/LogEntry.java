package project.cs.lisa.application.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

import project.cs.netinfutilities.UProperties;
import android.os.Environment;
import android.util.Log;

public class LogEntry {

    public static final String TAG = "LogEntry";

    public static final String LOG_FILE =
            UProperties.INSTANCE.getPropertyWithName("log.file");
    public static final String EXTERNAL_STORAGE =
            Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private String mTimestamp;
    private Type mType;
    private Action mAction;
    private long mStartTime;
    private long mStopTime;
    private long mTransferredBytes = 0;
    private boolean mFailed = false;

    public enum Type {
        UPLINK;
    }

    public enum Action {
        GET_WITH_FILE;
    }

    public LogEntry(Type type, Action action) {
        mTimestamp = DATE_FORMAT.format(Calendar.getInstance().getTime());
        mType = type;
        mAction = action;
        mStartTime = System.currentTimeMillis();
    }

    private void writeToFile() {
        try {
            FileUtils.write(new File(EXTERNAL_STORAGE + LOG_FILE), toString() + "\n", true);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write log entry to file");
        }
    }

    public void stop() {
        mStopTime = System.currentTimeMillis();
        writeToFile();
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

    public static boolean deleteLogFile() {
        return FileUtils.deleteQuietly(new File(EXTERNAL_STORAGE + LOG_FILE));
    }

}
