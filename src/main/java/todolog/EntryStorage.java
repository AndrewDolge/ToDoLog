package todolog;

import java.util.List;

/**
 * EntryStorage objects are able to save log entries in storage
 * 
 * 
 * @author Andrew Dolge
 * 
 * 
 * 
 *         ----- CHANGE LOG -------
 * 
 *         8/18/19 - created, added interface methods: addLogEntry getEntries
 * 
 *         8/22/19 - added taskName to addLogEntry
 * 
 * 
 * 
 */
public interface EntryStorage{

    /**
     * Adds a log entry for the given task into storage.
     * 
     * @param taskID the id of the task associated with the log entry
     * @param taskName the name of the task associated with the log entry
     * @param content the user generated content of the log entry
     * @param timestamp    the timestamp of the log entry
     * 
     */
    public void addLogEntry(String taskID, String taskName, String content, long timestamp);



    /**
     * Gets a list of entries from the given task.
     * 
     * 
     * @param taskID the id of the task
     * @return a list of log entries
     */
    public List<Entry> getEntries(String taskID);



}//EntryStorage