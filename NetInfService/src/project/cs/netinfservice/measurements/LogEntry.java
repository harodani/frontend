package project.cs.netinfservice.measurements;

public class LogEntry {
    public static final String TAG = "LogEntry";
    
    int type; // NRS, Bluetooth, Database
    int action; // Sending file, retrieving file
    String timestamp;
    long fileSize;
    
    public LogEntry() {
         
    }
}
