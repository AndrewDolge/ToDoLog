package todolog;

import java.util.Random;

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
 *  8/21/19 - Made into an immutable data object. Removed setters for data fields.
 *            added completed as construction parameter.
 * 
 */
public class Task {

    private boolean completed;
    private String id;
    private String name;

    public Task(String id, String name, boolean completed) {

        this.id = id;
        this.name = name;
        this.completed = false;
    }// constructor

    public String getName() {
        return name;
    }//getName


    public String getId() {
        return id;
    }//getID

    public boolean isCompleted() {
        return completed;
    }// isCompleted

    /**
     * Generates an ID for a task.
     * 
     * Similar to Habitica's Task IDs. 
     * 
     * DO NOT USE WHEN INTEGRATING WITH HABITICA.
     * 
     */
    public static String generateID(){

        return generateFourHex() + generateFourHex() + "-" + 
                                   generateFourHex() + "-" + 
                                   generateFourHex() + "-" + 
                                   generateFourHex() + "-" + 
                                   generateFourHex() + generateFourHex() + generateFourHex();
       

    }

    /**
     * Generates a random string with 4 hex characters.
     * 
     * @return
     */
    private static String generateFourHex(){
        return  Integer.toHexString(new Random().nextInt(65536));

    }

}// Task