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
     * updates the given entry in the system.
     * 
     * @param entry
     */
    public void updateEntry(Entry entry);

    /**
     * Gets a list of entries from the given taskID.
     * 
     * 
     * @param taskID the id of the task
     * @return a list of log entries
     */
    public List<Entry> findEntries(int taskID);

    /**
     * Retrieves the entry with the given id.
     * 
     * @param entryID
     * @return the entry with the given id, or null if the entry does not exist.
     */
    public Entry getEntry(int entryID);

    /**
     * Deletes the Entry in the system.
     * 
     * @throws IllegalArgumentException if the system does not have the given ID.
     * @param entryID
     */
    public void deleteEntry(int entryID);

}// EntryStorage