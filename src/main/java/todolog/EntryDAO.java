package todolog;

import java.util.List;

/**
 * EntryStorage objects are able to save log entries in storage
 * 
 * 
 * @author Andrew Dolge
 * 
 * 
 *         ----- CHANGE LOG -------
 * 
 *         8/18/19 - created, added interface methods: addLogEntry getEntries
 * 
 *         8/22/19 - added taskName to addLogEntry
 * 
 *         8/24/19 - changed to standard DAO pattern, after learning DAO patterns.
 * 
 * 
 */
public interface EntryDAO {

    /**
     * Adds a log entry for the given task into storage.
     * 
     * @param taskID    the id of the task associated with the log entry
     * @param content   the user generated content of the log entry
     * @param logTime the logTime of the log entry
     * 
     * 
     */
    public void addLogEntry(int taskID, String content, long logTime);

    /**
     * Adds a log entry for the given task into storage, at the current time.
     * @param taskID the id of the task associated with the log entry
     * @param content the user generated content of the log entry
     */
    public void addLogEntry(int taskID, String content);


    /**
     * updates the given entry in the system.
     * 
     * @param entry
     */
    public void updateEntry(Entry entry);


    /**
     * Retrieves the entry with the given id.
     * 
     * @param entryID
     * @return the entry with the given id, or null if the entry does not exist.
     */
    public Entry getEntry(int entryID);

    /**
     * 
     * Retrieves all entries from the system.
     * @return a list of all entries
     */
    public List<Entry> findEntries();


    /**
     * 
     * Retrieves all entries from the system with a timestamp between the two times, and with the given task ID.
     * 
     * 
     * @param taskID  the id of the task. defaults to any task.
     * @param minTime the minimum timestamp an entry should have. defaults to 0.
     * @param maxTime the maximum timestamp an entry should have. defaults to Long.MAX
     * 
     */
    public List<Entry> findEntries(Integer taskID, Long minTime, Long maxTime);

    /**
     * Deletes the Entry in the system.
     * 
     * @throws IllegalArgumentException if the system does not have the given ID.
     * @param entryID
     */
    public void deleteEntry(int entryID);

    /**
     * Deletes everything in the system, as if no entries were added.
     * 
     */
    public void resetEntries();

}// EntryStorage