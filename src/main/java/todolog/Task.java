package todolog;

import com.google.gson.Gson;


/**
 * Represents a task for the user. can be completed and reset.
 * 
 * Stores information about tasks.
 * 
 * 
 * @author Andrew Dolge
 * 
 * 
 * 
 *         ----- CHANGE LOG -------
 * 
 *         8/17/19 - Initialized, added getters and setters for: boolean
 *         completed long id String name
 * 
 *         8/21/19 - Made into an immutable data object. Removed setters for
 *         data fields. added completed as construction parameter.
 * 
 */

 public class Task {

    private boolean completed;
    private int  taskID;
    private String name;

    public Task(int taskID, String name, boolean completed) {

        this.taskID = taskID;
        this.name = name;
        this.completed = completed;
    }// constructor

    public String getName() {
        return name;
    }//getName

    public int getTaskID() {
        return taskID;
    }//getTaskID

    public boolean isCompleted() {
        return completed;
    }// isCompleted

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }//setCompleted

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){


        Gson gson = new Gson();

        return gson.toJson(this);
        /*
        JsonObject object = new JsonObject();

        object.addProperty("taskID", getTaskID());  
        object.addProperty("taskName", getName());  
        object.addProperty("completed", isCompleted()); 
        return object.toString();
        */ 

    


       

    }//toString



    
}// Task