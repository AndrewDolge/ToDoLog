package todolog;

/**
 * Represents a log entry created by the user.
 * 
 * Has an associated timestamp, user provided content string, and an associated task ID.
 * 
 * @author Andrew Dolge
 * 
 * -----  CHANGE LOG -------
 * 
 *  8/17/19 - Initialized, added getters and setter methods for:
 * 
 *              String content
 *              long   taskID
 *              long timestamp
 * 
 * 8/22/19 - Added TaskName, removed unnecessary getters.
 * 
 */
public class Entry{


    private String content;
    private String taskName;
    private String taskID;
    private long   timestamp;

    public Entry(String taskID, String taskName, String content, long timestamp){

        this.content = content;
        this.taskName = taskName;
        this.taskID  = taskID;
        this.timestamp = timestamp;

    }//Constructor
    

    public double getTimestamp() {
        return timestamp;
    }//getTimestamp

    public String getTaskID() {
        return taskID;
    }//getTaskID

    public String getTaskName(){
        return taskName;
    }//getTaskName

    public String getContent() {
        return content;
    }//getContent



}//class