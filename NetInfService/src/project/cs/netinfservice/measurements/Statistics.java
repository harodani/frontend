package project.cs.netinfservice.measurements;

import java.util.HashMap;

public class Statistics {
    /** Debug Tag */
    private static final String TAG = "Statistics";
    
    /** Statistics for counting number of access */
    private HashMap<String, Integer> numberOfAccess;
    
    // Just initializes a counter
    public Statistics() {
        
    } 
    /*
    // Specifies which type of Statistics we are going to keep
    public Statistics(String type) {
        Log.d(TAG, "Creating statistics for " + type);

        switch(type) {
            
        }
    }*/
}
