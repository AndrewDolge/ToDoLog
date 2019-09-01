package todolog;

import com.google.gson.JsonObject;


/**
 * Represents a log entry created by the user.
 * 
 * Has an associated logTime, user provided content string, and an associated task ID.
 * 
 * @author Andrew Dolge
 * 
 * -----  CHANGE LOG -------
 * 
 *  8/17/19 - Initialized, added getters and setter methods for:
 * 
 *              String content
 *              long   taskID
 *              long logTime
 * 
 * 8/22/19 - Added TaskName, removed unnecessary getters.
 * 
 * 8/23/19 - removed TaskName, after considering database design
 * 
 * 
 */
public class Entry{


    private String content;
    private int taskID;
    private int entryID;
    private long   logTime;

    public Entry(int entryID, int taskID, String content, long logTime){

        this.content   = content;
        this.taskID    = taskID;
        this.entryID   = entryID;
        this.logTime = logTime;

    }//Constructor
    
    public long getlogTime() {
        return logTime;
    }//getlogTime

    public int getTaskID() {
        return taskID;
    }//getTaskID

    public String getContent() {
        return content;
    }//getContent

    public int getEntryID() {
        return entryID;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public void setEntryID(int entryID) {
        this.entryID = entryID;
    }

    public void setlogTime(long logTime) {
        this.logTime = logTime;
    }


    public String toString(){

        JsonObject object = new JsonObject();

        object.addProperty("entryID", getEntryID()); 
        object.addProperty("taskID", getTaskID());  
        object.addProperty("content", getContent());  
        object.addProperty("logTime", getlogTime());  

        return object.toString();

    }


}//class