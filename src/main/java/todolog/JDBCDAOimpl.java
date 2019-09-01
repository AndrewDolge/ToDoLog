package todolog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores tasks and log entries into a sql database.
 * 
 * @author Andrew Dolge
 * 
 *         ---- CHANGE LOG ---- 8/22/2019 - implemented interface methods for
 *         EntryStorage and TaskStorage
 * 
 * 
 */

public class JDBCDAOimpl implements EntryDAO, TaskDAO {

    private String url;
    private long startTime;
    private long endTime;

   

    public JDBCDAOimpl(String url) {

        this(url, getStartOfDay().toEpochSecond(), getStartOfDay().plusDays(1).toEpochSecond());

    }

    public JDBCDAOimpl(String url, long startTime, long endTime) {
        this.url = url;
        this.startTime = startTime;
        this.endTime = endTime;
        


        //attempt to create the tables for the project
        try (Connection conn = connect();   Statement stmt = conn.createStatement()) {
            /**
             *  CREATE TABLE Tasks (                     
                taskID INTEGER PRIMARY KEY AUTOINCREMENT,
                taskName TEXT NOT NULL,                 
                active INTEGER NOT NULL DEFAULT 1        
                );
             */

           stmt.execute(
            "CREATE TABLE IF NOT EXISTS Tasks (       \n" +
            "taskID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "taskName TEXT NOT NULL,                  \n" + 
            "active INTEGER NOT NULL DEFAULT 1        \n" +
            ");"
            );


            /**
             * CREATE TABLE IF NOT EXISTS Entries (
            entryID   INTEGER PRIMARY KEY,
            content   TEXT,
            logTime NUMERIC NOT NULL,
            taskID    INTEGER NOT NULL
          )
             */
            stmt.execute("CREATE TABLE IF NOT EXISTS Entries (" 
            + "entryID   INTEGER PRIMARY KEY,\n"
            + "content   TEXT,\n" 
            + "logTime NUMERIC NOT NULL,\n" 
            + "taskID    INTEGER NOT NULL,\n"
            + "FOREIGN KEY (taskID) REFERENCES Tasks(taskID)" 
            + " \n);"
            );


            } catch (SQLException sqle) {

               

                throw new RuntimeException("JDBCDAOimpl.Constructor: SQL exception caught. error: " + sqle.getMessage(), sqle);
            }

    }// constructor

    /**
     * Establishes a connection to the database.
     * 
     * 
     * @return a connection to the database.
     */
    private Connection connect() {

        Connection conn = null;

            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection(this.url);

            } catch (SQLException sqle) {
                System.out.println("JDBCDAOimpl.connect(): SQLException caught: " + sqle.getMessage());
                sqle.printStackTrace();
                
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        

        return conn;

    }// connect



    @Override
    public void addTask(String taskName) {

        try (Connection conn = connect()) {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO Tasks(taskName) VALUES(?)");

            statement.setString(1, taskName);

            statement.executeUpdate();

        } catch (SQLException sqle) {
            
            throw new RuntimeException("JDBCDAOimpl.addTask: SQL exception caught. error: " + sqle.getMessage(), sqle);
        }

    }// addTask

    /**
     * Gets the task with the given taskID.
     * 
     * @return the task with the given taskID, if it exists. Null otherwise.
     */
    @Override
    public Task getTask(int taskID) {

        // for every task in all tasks
        for (Task t : this.getTasks()) {
            if (t.getTaskID() == taskID) {
                // return the task with the taskID
                return t;
            }
        }

        return null;

    }// getTask

    /**
     * Gets all tasks in the system.
     * 
     * @return a list of all tasks in the system.
     * 
     */
    @Override
    public List<Task> getTasks() {

        ArrayList<Task> tasks = new ArrayList<Task>();
        try (Connection conn = connect()) {

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM Tasks WHERE active = 1");
            ResultSet taskData = statement.executeQuery();

          

            // adds all tasks that returned from the query to the list
            while (taskData.next()) {


                PreparedStatement completedStatement = conn.prepareStatement(
                                    "SELECT taskID, logTime\n" +
                                    "FROM Entries \n" +  
                                    "WHERE taskID = ? AND logTime <= ? AND logTime > ?"
                                    );


                completedStatement.setInt (1, taskData.getInt("taskID"));
                completedStatement.setLong(2, this.startTime);
                completedStatement.setLong(3, this.endTime);

                ResultSet completed = completedStatement.executeQuery();

                tasks.add(
                    new Task(
                        taskData.getInt("taskID"),
                        taskData.getString("taskName"),
                        completed.next()));
            } // while

        } catch (SQLException sqle) {

            throw new RuntimeException("JDBCDAOimpl.getTasks: SQL exception caught. error: " + sqle.getMessage(), sqle);
        }

        return tasks;

    }// getTasks

    @Override
    public void addLogEntry(int taskID, String content, long logTime) {

        try (Connection conn = connect()) {
            PreparedStatement statement = conn
                    .prepareStatement("INSERT INTO Entries(taskID, content, logTime) VALUES(?,?,?)");

            statement.setInt(0, taskID);
            statement.setString(1, content);
            statement.setLong(2, logTime);

            statement.executeUpdate();

        } catch (SQLException sqle) {
            // TODO handle sql exception
        }

    }// addLogEntry

    @Override
    public List<Entry> findEntries(int taskID) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        try (Connection conn = connect()) {

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM Entries WHERE taskID = ?");
            statement.setInt(0, taskID);

            // TODO log getEntry result
            ResultSet entryData = statement.executeQuery();

            // adds all tasks that returned from the query to the list
            while (entryData.next()) {

                entries.add(new Entry(entryData.getInt("entryID"), entryData.getInt("taskID"),
                        entryData.getString("content"), entryData.getLong("logTime")

                ));
            } // while

        } catch (SQLException sqle) {
            // TODO handle sql exception
        }

        return entries;
    }// getEntries

    @Override
    public void updateTask(Task task) {

        try (Connection conn = connect()) {

            PreparedStatement stmt = conn.prepareStatement("UPDATE Tasks\n" + "SET taskName = ?" + "WHERE taskID = ?");

            stmt.setString(0, task.getName());
            stmt.setInt(1, task.getTaskID());

            // The user is completing the task without
            // add a dummy entry to the database
            if (!getTask(task.getTaskID()).isCompleted() && task.isCompleted()) {

                addLogEntry(task.getTaskID(), "", System.currentTimeMillis());

                // The user is uncompleting task
                // remove all tasks from the database
            } else if (!getTask(task.getTaskID()).isCompleted() && task.isCompleted()) {

                // TODO update this for loop when findEntries with logTime gets implemented
                for (Entry e : findEntries(task.getTaskID())) {

                    if (startTime <= e.getlogTime() && e.getlogTime() < endTime) {
                        deleteEntry(e.getEntryID());
                    } // if

                } // for

            } // else

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // catch

    }// updateTAsk

    @Override
    public void deleteTask(int taskID) {


        try(Connection conn = connect()){




        }catch(SQLException sqle){
            sqle.printStackTrace();

        }

    }// deleteTask

    @Override
    public void updateEntry(Entry entry) {

    }// updateEntry

    @Override
    public Entry getEntry(int entryID) {

        Entry result = null;

        try (Connection conn = connect()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM Entries WHERE entryID = ?");
            statement.setInt(0, entryID);

            ResultSet entryData = statement.executeQuery();

            result = new Entry(entryData.getInt("entryID"), entryData.getInt("taskID"), entryData.getString("content"),
                    entryData.getLong("logTime"));

        } catch (SQLException sqle) {
            // TODO handle sql exception in getEntry
        }

        return result;

    }

    @Override
    public void deleteEntry(int entryID) {

    }

    private static ZonedDateTime getStartOfDay() {
        return ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay(ZoneId.systemDefault());
    }

    @Override
    public void removeTask(int taskID) {

    }

}// class