package todolog;



/**
 * Main interface for the ToDoLog System. 
 * 
 * 
 * @author Andrew Dolge
 * 
 * 
 * 
 * -----  CHANGE LOG -------
 * 
 *  8/17/19 - Initialized, added interface methods:
 *      addTask
 *      completeTask
 *      resetTasks
 *   
 * 
 * 
 */
public interface TDLTracker{

    /**
     * 
     * Adds a new task to the System. 
     * 
     * 
     * @param taskName the name of the task
     * @return the id assigned to the task by the system.
     * @throws IllegalArgumentException if the taskName is not valid
     * 
     * 
     */
    public int addTask(String taskName);

    /**
     * Completes the given task, storing the log entry into the system.
     * 
     * 
     * @param id - The id of the task.
     * @param logEntry - The user's log entry for the task completion.
     * @throws IllegalArgumentException if no task with the id exists, or if the logEntry is not valid.
     */
    public void completeTask(int id, String logEntry);


    /**
     * Resets all tasks to the uncompleted state. Used to reset daily tasks at Chron.
     * 
     */
    public void resetTasks();


    /**
     * Gets a list of all tasks from the system. 
     * 
     * @return an array of tasks from the system.
     */
    public Task[] getTasks();
    
    
    /**
     * Retrieves all log entries associated with the given task.
     * 
     * @param taskID the id of the task
     * @return a list of all log entries with the given task.
     * @throws IllegalArgumentException if the taskID is not valid
     */
    public Entry[] getEntries(long taskID);



}