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
 *         8/24/2019 - Changed to standard DAO pattern, after learning about DAO patterns.
 * 
 * 
 * 
 */
public interface TaskDAO{

    /**
     * Adds a task into storage.
     * 
     * @param name the name of the task
     * @return the id of the task
     */
    public void addTask(String taskName);

    /**
     * marks the given task as complete.
     * 
     * @param id the id of the task.
     * @throws IllegalArgumentException if the ID is not found. 
     * 
     */
    public void updateTask(Task task);

    /**
     * Gets a list of all tasks
     * 
     * @return an array of tasks. 
     */
    public List<Task> getTasks();

    /**
     * Gets the entry with the specific ID
     * @param taskID the id of the task
     */
    public Task getTask(int taskID);

    /**
     * removes the task from the daily tasks.
     * 
     * @param taskID the id of the task to be deleted.
     */
    public void removeTask(int taskID);


    /**
     * Deletes a task from the system.
     */
    public void deleteTask(int taskID);


    /**
     * Deletes everything in the system, as if no tasks were added.
     * 
     */
    public void resetTasks();


}//interface