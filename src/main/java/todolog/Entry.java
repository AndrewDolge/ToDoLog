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
 *              double timestamp
 * 
 */
public class Entry{


    private String content;
    private long taskID;
    private double timestamp;

    public Entry(long taskID, String content, double timestamp){
        setContent(content);
        setTaskID(taskID);
        setTimestamp(timestamp);
    }//Constructor
    

    public double getTimestamp() {
        return timestamp;
    }//getTimestamp

    public long getTaskID() {
        return taskID;
    }//getTaskID

    private void setTaskID(long taskID) {
        this.taskID = taskID;
    }//setTaskID

    public String getContent() {
        return content;
    }//getContent

    public void setContent(String content) {
        this.content = content;
    }//setContent

    private void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }//setTimeStamp





}//class