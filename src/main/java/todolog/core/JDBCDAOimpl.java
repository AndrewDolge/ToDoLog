package todolog.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import todolog.util.TimeUtil;

/**
 * Stores tasks and log entries into a sql database.
 * 
 * 
 * @author Andrew Dolge
 * 
 *         ---- CHANGE LOG ---- 8/22/2019 - implemented interface methods for
 *         EntryStorage and TaskStorage
 * 
 * 
 */

public class JDBCDAOimpl implements EntryDAO, TaskDAO {

    private static Logger log = LogManager.getLogger(JDBCDAOimpl.class.getName());

    private String url;
    private long startTime;
    private long endTime;

    public JDBCDAOimpl(String url) {

        this(url, TimeUtil.getDayStart(),TimeUtil.getNextDayStart());

    }

    public JDBCDAOimpl(String url, long startTime, long endTime) {
        this.url = url;
        this.startTime = startTime;
        this.endTime = endTime;

        createTasksTable(false);
        createEntriesTable(false);

    }// constructor

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

        Task result = null;

        try (Connection conn = connect()) {

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM Tasks WHERE taskID = ?");
            statement.setInt(1, taskID);
            ResultSet taskData = statement.executeQuery();
            taskData.next();

            result = new Task(taskData.getInt("taskID"), taskData.getString("taskName"), isTaskCompleted(taskID));

        } catch (SQLException sqle) {

            log.info(sqle.toString());

        }

        return result;

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

                boolean completed = isTaskCompleted(taskData.getInt("taskID"));

                tasks.add(new Task(taskData.getInt("taskID"), taskData.getString("taskName"), completed));
            } // while

        } catch (SQLException sqle) {

            throw new RuntimeException("JDBCDAOimpl.getTasks: SQL exception caught. error: " + sqle.getMessage(), sqle);
        }

        return tasks;

    }// getTasks

    @Override
    public void updateTask(Task task) {

        try (Connection conn = connect()) {

            PreparedStatement stmt = conn.prepareStatement("UPDATE Tasks\n" + "SET taskName = ?,\n" +"active = ?\n"  + "WHERE taskID = ?");

            stmt.setString(1, task.getName()         );
            stmt.setInt   (2, task.isActive() ? 1:0  );
            stmt.setInt   (3, task.getTaskID()       );
           
            stmt.executeUpdate();

            // The user is completing the task without
            // add a dummy entry to the database
            if (!getTask(task.getTaskID()).isCompleted() && task.isCompleted()) {

                addLogEntry(task.getTaskID(), "", this.startTime);

                // The user is uncompleting task
                // remove all tasks from the database
            } else if (getTask(task.getTaskID()).isCompleted() && !task.isCompleted()) {

                for (Entry e : findEntries(task.getTaskID(), startTime, endTime)) {

                    deleteEntry(e.getEntryID());

                } // for

            } // else

        } catch (SQLException e) {

            log.info(e.toString());

        } // catch

    }// updateTask

    @Override
    public void removeTask(int taskID) {

        try (Connection conn = connect()) {

            PreparedStatement statement = conn
                    .prepareStatement("UPDATE Tasks\n" + "SET active = 0\n" + "WHERE taskID = ?;");

            statement.setInt(1, taskID);

            statement.executeUpdate();

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

    }//removeTask

    @Override
    public void deleteTask(int taskID) {

        try (Connection conn = connect()) {

            PreparedStatement statement = conn.prepareStatement("DELETE FROM Tasks\n" + "WHERE taskID = ?;");

            statement.setInt(1, taskID);

            statement.executeUpdate();

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

    }// deleteTask

    @Override
    public void addLogEntry(int taskID, String content) {
       addLogEntry(taskID, content,TimeUtil.getNow());
    }//addLogEntry

    @Override
    public void addLogEntry(int taskID, String content, long logTime) {

        try (Connection conn = connect()) {
            PreparedStatement statement = conn
                    .prepareStatement("INSERT INTO Entries(taskID, content, logTime) VALUES(?,?,?)");

            statement.setInt(1, taskID);
            statement.setString(2, content);
            statement.setLong(3, logTime);

            statement.executeUpdate();

        } catch (SQLException sqle) {

            log.info(sqle.toString());
        }

    }// addLogEntry

    @Override
    public List<Entry> findEntries() {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        try (Connection conn = connect()) {

            PreparedStatement statement = conn.prepareStatement("SELECT * FROM Entries");

            ResultSet entryData = statement.executeQuery();

            // adds all tasks that returned from the query to the list
            while (entryData.next()) {

                entries.add(new Entry(entryData.getInt("entryID"), entryData.getInt("taskID"),
                        entryData.getString("content"), entryData.getLong("logTime")

                ));
            } // while

        } catch (SQLException sqle) {

            log.info(sqle.toString());
        }

        return entries;
    }//findEntries

    public List<Entry> findEntries(int taskID) {
        return findEntries(taskID, null, null);
    }// findEntries

    public List<Entry> findEntries(Long minTime, Long maxTime) {
        return findEntries(null, minTime, maxTime);
    }// findEntries

    @Override
    public List<Entry> findEntries(Integer taskID, Long minTime, Long maxTime) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        String sql;

        if (taskID == null) {
            sql = "SELECT * FROM Entries WHERE ? <=  logTime AND logTime < ?";

        } else {
            sql = "SELECT * FROM Entries WHERE ? <=  logTime AND logTime < ? AND taskID = ?";
        }

        if (minTime == null) {
            minTime = (long) 0;
        }
        if (maxTime == null) {
            maxTime = Long.MAX_VALUE;
        }

        try (Connection conn = connect()) {

            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1, minTime);
            statement.setLong(2, maxTime);
            if (taskID != null) {
                statement.setInt(3, taskID);
            }

            ResultSet entryData = statement.executeQuery();

            // adds all tasks that returned from the query to the list
            while (entryData.next()) {

                entries.add(new Entry(entryData.getInt("entryID"), entryData.getInt("taskID"),
                        entryData.getString("content"), entryData.getLong("logTime")

                ));
            } // while

        } catch (SQLException sqle) {

            log.info(sqle.toString());

        }

        return entries;
    }//findEntries

    @Override
    public void updateEntry(Entry entry) {

        try (Connection conn = connect()) {

            PreparedStatement statement = conn.prepareStatement("UPDATE Entries    \n" + "SET taskID  =   ?,\n"
                    + "    content =   ?,\n" + "    logTime =   ? \n" + "WHERE entryID = ?;");

            statement.setInt(1, entry.getTaskID());
            statement.setString(2, entry.getContent());
            statement.setLong(3, entry.getLogTime());
            statement.setInt(4, entry.getEntryID());

            statement.executeUpdate();

        } catch (SQLException e) {

            log.info(e.toString());
        }

    }// updateEntry

    @Override
    public Entry getEntry(int entryID) {

        Entry result = null;

        try (Connection conn = connect()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM Entries WHERE entryID = ?");
            statement.setInt(1, entryID);

            ResultSet entryData = statement.executeQuery();

            result = new Entry(entryData.getInt("entryID"), entryData.getInt("taskID"), entryData.getString("content"),
                    entryData.getLong("logTime"));

        } catch (SQLException sqle) {

            log.info(sqle.toString());
        }

        return result;

    }//getEntry

    @Override
    public void deleteEntry(int entryID) {

        try (Connection conn = connect()) {

            PreparedStatement statement = conn.prepareStatement("DELETE FROM Entries\n" + "WHERE entryID = ?");

            statement.setInt(1, entryID);
            statement.executeUpdate();

        } catch (SQLException e) {

            log.info(e.toString());
        }

    }//deleteEntry

    @Override
    public void resetTasks() {
        createTasksTable(true);

    }// resetTasks

    @Override
    public void resetEntries() {
        createTasksTable(true);

    }// resetEntries

    /**
     * creates the Tasks Table.
     * 
     * @param overwrite if true, will overwrite an existing table.
     */
    private void createTasksTable(boolean overwrite) {

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            if (overwrite) {

                stmt.execute("DROP TABLE IF EXISTS Tasks;");

                stmt.execute("CREATE TABLE  Tasks (       \n" + "taskID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + "taskName TEXT NOT NULL,                  \n" + "active INTEGER NOT NULL DEFAULT 1        \n"
                        + ");");

            } else {

                stmt.execute("CREATE TABLE IF NOT EXISTS Tasks (       \n"
                        + "taskID INTEGER PRIMARY KEY AUTOINCREMENT,\n" + "taskName TEXT NOT NULL,                  \n"
                        + "active INTEGER NOT NULL DEFAULT 1        \n" + ");");

            } // else

        } catch (SQLException sqle) {

            throw new RuntimeException("JDBCDAOimpl.createTaskTable: SQL exception caught. error: " + sqle.getMessage(),
                    sqle);
        } // sqle
    }// createTasksTable

    /**
     * creates the Tasks Table.
     * 
     * @param overwrite if true, will overwrite an existing table.
     */
    private void createEntriesTable(boolean overwrite) {

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            if (overwrite) {

                stmt.execute("DROP TABLE IF EXISTS Entries;");

                stmt.execute("CREATE TABLE  Entries (" + "entryID   INTEGER PRIMARY KEY,\n" + "content   TEXT,\n"
                        + "logTime NUMERIC NOT NULL,\n" + "taskID    INTEGER NOT NULL,\n"
                        + "FOREIGN KEY (taskID) REFERENCES Tasks(taskID)\n" + "ON DELETE CASCADE" + " \n);");
            } else {

                stmt.execute("CREATE TABLE IF NOT EXISTS Entries (" + "entryID   INTEGER PRIMARY KEY,\n"
                        + "content   TEXT,\n" + "logTime NUMERIC NOT NULL,\n" + "taskID    INTEGER NOT NULL,\n"
                        + "FOREIGN KEY (taskID) REFERENCES Tasks(taskID)\n" + "ON DELETE CASCADE" + " \n);");

            } // else

        } catch (SQLException sqle) {

            throw new RuntimeException(
                    "JDBCDAOimpl.createEntriesTable: SQL exception caught. error: " + sqle.getMessage(), sqle);
        } // sqle
    }// createTasksTable

    /**
     * Checks to see if the task has log entries between the given start and end
     * times.
     * 
     * @param taskID the id of the task
     * @return true, if there are log entries with the given taskID between the
     *         start and end times, false otherwise.
     * 
     * 
     */
    private boolean isTaskCompleted(int taskID) {

        boolean result = false;

        try (Connection conn = connect()) {

            // query if a log entry exists for the given logTime
            PreparedStatement completedStatement = conn.prepareStatement("SELECT taskID, logTime\n" + "FROM Entries \n"
                    + "WHERE taskID = ? AND ? <= logTime AND logTime < ?");

            completedStatement.setInt(1, taskID);
            completedStatement.setLong(2, this.startTime);
            completedStatement.setLong(3, this.endTime);

            ResultSet completed = completedStatement.executeQuery();
            result = completed.next();

        } catch (SQLException e) {

            log.info(e.toString());
        }

        return result;

    }// isTaskCompleted

    /**
     * Gets a ZonedDateTime with the time at the start of the current day.
     * 
     */


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
            conn.prepareStatement("PRAGMA foreign_keys=ON").execute();

        } catch (SQLException sqle) {
            System.out.println("JDBCDAOimpl.connect(): SQLException caught: " + sqle.getMessage());
            sqle.printStackTrace();

        } catch (ClassNotFoundException e) {

            log.info(e.toString());
        }

        return conn;

    }// connect



}// class