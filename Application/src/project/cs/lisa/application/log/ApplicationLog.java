package project.cs.lisa.application.log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import project.cs.lisa.application.log.LogEntry.Action;
import project.cs.lisa.application.log.LogEntry.Type;
import project.cs.netinfutilities.UProperties;
import android.os.Environment;

public class ApplicationLog {
    /** Debug Tag */
    public static final String TAG = "NetInfLog";

    public static final String logFile =
            UProperties.INSTANCE.getPropertyWithName("log.file");

    public static final String externalStorage =
            Environment.getExternalStorageDirectory().getAbsolutePath();

    public static ArrayList<LogEntry> mLog = new ArrayList<LogEntry>();

    public static LogEntry start(Type type, Action action) {
        LogEntry logEntry = new LogEntry(type, action);
        mLog.add(logEntry);
        return logEntry;
    }

    public static void stop(LogEntry logEntry, String hash, byte[] fileData) {
        logEntry.stop(hash, fileData);
    }

    public static void failed(LogEntry logEntry) {
        logEntry.failed();
    }

    public static String printLog() {
        StringBuilder builder = new StringBuilder();
        for (LogEntry log : mLog) {
            builder.append(log.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    public static void clearLog() {
        mLog.clear();
    }

    public static boolean writeLog() {
        try {
            FileUtils.write(new File(externalStorage + logFile), printLog(), true);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean deleteLog() {
        return FileUtils.deleteQuietly(new File(externalStorage + logFile));
    }
}
