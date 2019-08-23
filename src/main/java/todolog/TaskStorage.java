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
 *         8/19/19 - Initialized, added interface methods: addTask completeTask
 *         getTasks resetTasks
 * 
 * 
 * 
 */
public interface TaskStorage{

    /**
     * Adds a task into storage.
     * 
     * 
     * @param name the name of the task
     * @return the id of the task
     */
    public String addTask(String name);

    /**
     * marks the given task as complete.
     * 
     * @param id the id of the task.
     * @throws IllegalArgumentException if the ID is not found. 
     * 
     */
    public void completeTask(String taskID);

    /**
     * Gets a list of all tasks
     * 
     * @return an array of tasks. 
     */
    public List<Task> getTasks();

    /**
     * Resets all stored tasks to uncompleted.
     * 
     */
    public void resetTasks();



}//interface