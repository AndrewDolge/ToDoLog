package todolog;


/**
 * Represents a task for the user.
 * can be completed and reset.
 * 
 * Stores information about tasks. 
 * 
 * 
 * @author Andrew Dolge
 * 
 * 
 * 
 * -----  CHANGE LOG -------
 * 
 *  8/17/19 - Initialized, added getters and setters for:
 *          boolean completed
 *          long    id
 *          String  name
 * 
 */
public class Task {

    private boolean completed;
    private long id;
    private String name;

    public Task(long id, String name) {

        this.setId(id);
        this.setName(name);
        setCompleted(false);
    }// constructor

    public String getName() {
        return name;
    }//getName

    private void setName(String name){
        this.name = name;
    }

    public long getId() {
        return id;
    }//getID

    private void setId(long id) {
        this.id = id;
    }//setID

    public boolean isCompleted() {
        return completed;
    }// isCompleted

    private void setCompleted(boolean completed) {
        this.completed = completed;
    }// setCompleted

    /**
     * sets the task to completed.
     * 
     */
    private void complete() {
        setCompleted(true);
    }// complete

    /**
     * resets the task so it can be completed. "uncompletes" the task.
     */
    private void reset() {
        setCompleted(false);
    }

}// Task