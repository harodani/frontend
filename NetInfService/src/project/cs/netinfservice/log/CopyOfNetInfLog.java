//package project.cs.netinfservice.log;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//
//import netinf.common.datamodel.Identifier;
//import netinf.common.datamodel.InformationObject;
//
//import org.apache.commons.io.FileUtils;
//
//import project.cs.netinfservice.log.LogEntry.Action;
//import project.cs.netinfservice.log.LogEntry.Type;
//import project.cs.netinfutilities.UProperties;
//import android.os.Environment;
//
//public class CopyOfNetInfLog {
//    /** Debug Tag */
//    public static final String TAG = "NetInfLog";
//
//    public static final String logFile =
//            UProperties.INSTANCE.getPropertyWithName("log.file");
//
//    public static final String externalStorage =
//            Environment.getExternalStorageDirectory().getAbsolutePath();
//
//
//    public static ArrayList<LogEntry> mLog = new ArrayList<LogEntry>();
//
//    public static LogEntry start(String hash, Type type, Action action) {
//        LogEntry logEntry = new LogEntry(hash, type, action);
//        mLog.add(logEntry);
//        return logEntry;
//    }
//
//    public static LogEntry start(InformationObject io, Type type, Action action) {
//        LogEntry logEntry = new LogEntry(io, type, action);
//        mLog.add(logEntry);
//        return logEntry;
//    }
//
//    public static LogEntry start(Identifier identifier, Type type, Action action) {
//        LogEntry logEntry = new LogEntry(identifier, type, action);
//        mLog.add(logEntry);
//        return logEntry;
//    }
//
//    public static void stop(LogEntry logEntry, InformationObject io) {
//        logEntry.stop(io);
//    }
//
//    public static void stop(LogEntry logEntry, byte[] fileData) {
//        logEntry.stop(fileData.length);
//    }
//
//    public static void stop(LogEntry logEntry) {
//        logEntry.stop();
//    }
//
//    public static void failed(LogEntry logEntry) {
//        logEntry.failed();
//    }
//
//    public static String printLog() {
//        StringBuilder builder = new StringBuilder();
//        for (LogEntry log : mLog) {
//            builder.append(log.toString());
//            builder.append("\n");
//        }
//        return builder.toString();
//    }
//
//    public static void clearLog() {
//        mLog.clear();
//    }
//
//    public static boolean writeLog() {
//        try {
//            FileUtils.write(new File(externalStorage + logFile), printLog(), true);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        return true;
//    }
//
//    public static boolean deleteLog() {
//        return FileUtils.deleteQuietly(new File(externalStorage + logFile));
//    }
//}
