package todolog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;


/**
 * Stores tasks and log entries into a sql database.
 * 
 * @author Andrew Dolge
 * 
 *  ---- CHANGE LOG ----
 *  8/22/2019 - implemented interface methods for EntryStorage and TaskStorage
 * 
 * 
 */

public class JDBCStorage implements EntryStorage, TaskStorage {

    private String url;

    /**
     * 
     * 
     */
    public JDBCStorage(String url) {
        this.url = url;
    }// constructor

    /**
     * Establishes a connection to the data base.
     * 
     * 
     * @return a connection to the database.
     */
    private Connection connect() {

        Connection conn = null;

        try {
            conn = DriverManager.getConnection(this.url);

        } catch (SQLException sqle) {
            // TODO handle exception
        } // catch

        return conn;

    }// connect

    @Override
    public String addTask(String name) {

        String taskID;

        try (Connection conn = connect()) {
            PreparedStatement statement = conn
                    .prepareStatement("INSERT INTO TASKS(taskID, name, completed) VALUES(?,?,0)");

            do {
               taskID = Task.generateID();
            } while (this.getTask(taskID) != null);

            statement.setString(0,  taskID);
            statement.setString(1, name);

            statement.executeUpdate();

            return taskID;

        } catch (SQLException sqle) {
            // TODO handle sql exception
        }

        return null;
    }//addTask

    @Override
    public void completeTask(String taskID) {

        try (Connection conn = connect()) {
            PreparedStatement statement = conn.prepareStatement(
                            "UPDATE TASKS" +
                            "SET completed = 1" + 
                            "WHERE taskID = ?");

            //TODO log update result
            statement.executeUpdate();

        } catch (SQLException sqle) {
            // TODO handle sql exception
        }
    }//completeTask

    /**
     * Gets the task with the given taskID.
     * 
     * @return the task with the given taskID, if it exists. Null otherwise.
     */
    public Task getTask(String taskID) {

        // for every task in all tasks
        for (Task t : this.getTasks()) {
            if (t.getId() == taskID) {
                // return the task with the taskID
                return t;
            }
        }

        return null;

    }// getTask

    @Override
    public List<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<Task>();
        try (Connection conn = connect()) {

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM TASKS");
            //TODO log gettask query result
            ResultSet taskData = statement.executeQuery();
            
            //adds all tasks that returned from the query to the list
            while(taskData.next()){

                tasks.add(
                    new Task(
                        taskData.getString("taskID"),
                        taskData.getString("name"),
                        taskData.getBoolean("completed")
                        )    
                    );
            }//while

        } catch (SQLException sqle) {
            // TODO handle sql exception
        }

        return tasks;

    }//getTasks

    @Override
    public void resetTasks() {

        try (Connection conn = connect()) {
            PreparedStatement statement = conn.prepareStatement(
                            "UPDATE TASKS" +
                            "SET completed = 0" + 
                            "WHERE completed = 1");

            //TODO log update result for reset tasks
            statement.executeUpdate();

        } catch (SQLException sqle) {
            // TODO handle sql exception
        }

    }

    @Override
    public void addLogEntry(String taskID, String taskName, String content, long timestamp) {

        try (Connection conn = connect()) {
            PreparedStatement statement = conn
                    .prepareStatement("INSERT INTO ENTRIES(taskID, taskName, content, timestamp) VALUES(?,?,0)");

            do {
               taskID = Task.generateID();
            } while (this.getTask(taskID) != null);

            statement.setString(0,  taskID);
            statement.setString(1,  taskName);
            statement.setString(2,  content);
            statement.setLong  (3,  timestamp);

            statement.executeUpdate();

        } catch (SQLException sqle) {
            // TODO handle sql exception
        }

    }//addLogEntry

    @Override
    public List<Entry>  getEntries(String taskID) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        try (Connection conn = connect()) {

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM ENTRIES");

            //TODO log getEntry result
            ResultSet entryData = statement.executeQuery();
            
            //adds all tasks that returned from the query to the list
            while(entryData.next()){

               entries.add(
                    new Entry(
                        entryData.getString("taskID"),
                        entryData.getString("taskName"),
                        entryData.getString("content"),
                        entryData.getLong("timestamp")
                        
                        )
                    );
            }//while

        } catch (SQLException sqle) {
            // TODO handle sql exception
        }

        return entries;
    }//getEntries

}//class