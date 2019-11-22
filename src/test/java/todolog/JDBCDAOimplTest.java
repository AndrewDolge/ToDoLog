package todolog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import todolog.core.*;

/**
 * Testing class for the JDBCDAOimpl class.
 * 
 * 
 */
//@Ignore 
public class JDBCDAOimplTest{

    public JDBCDAOimplTest(){}

    private static Logger log = Logger.getLogger(JDBCDAOimplTest.class.getName());

    public static String getTestDBURL(){
        return "jdbc:sqlite:test.db";
        //return "jdbc:sqlite::resource:test.db";
    }

    public static void fillDBWithEntries(JDBCDAOimpl db, long time){
        for(Entry e: getExpectedEntries(time)){
            db.addLogEntry(e.getTaskID(), e.getContent(), e.getLogTime());
        }
    }

    public static void fillDBWithTasks( JDBCDAOimpl db){

        for(Task t: getExpectedTasks()){
            db.addTask(t.getName());
        }

    }

    public static List<Task> getExpectedTasks(){

        List<Task> expected = new ArrayList<Task>();
        expected.add(new Task(1,"laundry",false));
        expected.add(new Task(2,"sweeping",false));
        expected.add(new Task(3,"homework",false));  
        return expected;
    }

    public static List<Task> getExpectedTasksCompleted(){
        List<Task> expected = new ArrayList<Task>();
        for(Task t: getExpectedTasks()){
            t.setCompleted(true);
            expected.add(t);
        }
        return expected;
  
    }

    public static List<Entry> getExpectedEntries(long logTime){
        return getExpectedEntries(logTime, 1);
    }

    public static List<Entry> getExpectedEntries(long logTime, int id){
        List<Entry> expected = new ArrayList<Entry>();

        for(Task t: getExpectedTasks()){

            expected.add(new Entry(id, t.getTaskID(), Integer.toString(id), logTime));
            id++;
            expected.add(new Entry(id, t.getTaskID(), Integer.toString(id), logTime));
            id++;
        }

        return expected;
    }

    @After
    public void clearDB(){


        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            log.info(e.toString());
        }

        try(Connection conn = DriverManager.getConnection(getTestDBURL())){
           

          conn.createStatement().executeUpdate("DROP TABLE IF EXISTS Tasks;");
          conn.createStatement().executeUpdate("DROP TABLE IF EXISTS Entries;");

        }catch(SQLException sqle){
            throw new RuntimeException("JDBCDAOimplTest.clearMemoryDB: SQL exception caught. error: " + sqle.getMessage(), sqle);
        }



    } 

    /**
     * Compare two lists of entries
     */
    public void compareEntries(List<Entry> expected, List<Entry> actual){

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());

        for(int i = 0; i < actual.size(); i++){
            assertEquals(expected.get(i).toString(), actual.get(i).toString());
        }

    }//compareEntries
   
    /**
     * compare two lists of tasks
     */
    public void compareTasks(List<Task> expected, List<Task> actual){

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());

        for(int i = 0; i < actual.size(); i++){
            assertEquals(expected.get(i).toString(), actual.get(i).toString());
        }

    }


//*************************TESTS************************

    /**
     * Tests if the database can be created and set up correctly.
     * 
     * 
     */
    @Test
    public void testDBCreation(){

      
        JDBCDAOimpl db = new JDBCDAOimpl(getTestDBURL());

    }

    /**
     * Tests the creation of new tasks.
     * 
     */
    @Test
    public void testTaskCreation(){


        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url);

        fillDBWithTasks(db);

        List<Task> tasks = db.getTasks();

        compareTasks(getExpectedTasks(), tasks);

    }//testTaskCreation


    /**
     * 
     * Tests to see that if Entries outside of the current day count towards completing the task.
     * Does the task's daily timer "rollover?"
     * 
     */
    @Test
    public void testTaskRollover(){

        JDBCDAOimpl db = new JDBCDAOimpl(getTestDBURL(), 2, 3);

        fillDBWithTasks(db);
        fillDBWithEntries(db, 1);

        compareTasks(getExpectedTasks(), db.getTasks());
            
     

    
    }//testTaskRollover

    /**
     * 
     * 
     * Tests whether or not the task is completed when a log entry is added within the current "day"
     * 
     */
    @Test
    public void testTaskCompletion(){

        JDBCDAOimpl db = new JDBCDAOimpl(getTestDBURL(), 1, 3);

        fillDBWithTasks(db);
        fillDBWithEntries(db, 2);

        compareTasks(getExpectedTasksCompleted(), db.getTasks());
            
    
    }//testTaskCompletion

    /**
     * Test the removeTask() method
     * This should remove it from the list of all entries, but should be accessible with the id.
     * 
     * 
     */
    @Test
    public void testTaskRemoval(){

        JDBCDAOimpl db = new JDBCDAOimpl(getTestDBURL());
        fillDBWithTasks(db);
        db.removeTask(3);

        List<Task> expected = getExpectedTasks();
        expected.remove(2);

        compareTasks( expected, db.getTasks());
        assertNotNull(db.getTask(3));

      
        
    }//testTaskRemoval

    /**
     * 
     * 
     * Tests the deleteTask() method.
     * This should remove the task from both the list of all entries, and getTask(id) should return null.
     * 
     */
    @Test
    public void testTaskDeletion(){

        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url);

        fillDBWithTasks(db);
        db.deleteTask(3);
        
        List<Task> expected = getExpectedTasks();
        expected.remove(2);

        compareTasks(expected, db.getTasks());
        //confirm that the task is gone from the system.
        assertNull(db.getTask(3));

        
    }//testTaskCreation

    /**
     * 
     * test the updateTask() method, including completing the task and changing the name.
     * 
     * 
     */
    @Test
    public void testTaskUpdate(){

        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url,0,2);

        fillDBWithTasks(db);
        
        //complete the task and change the name
        Task completedTask = new Task(1, "completed!", true);
        db.updateTask(completedTask);

        //get the updated task and compare them
        Task updatedTask = db.getTask(1);
        assertEquals(completedTask.toString(), updatedTask.toString() );

    }//testTaskUpdate

    /**
     * Tests uncompleting a task with a log entry created by a task update
     * 
     */
    @Test
    public void testTaskUpdateUncomplete(){

        
        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url,0,2);
        fillDBWithTasks(db);

        //complete the task
        db.updateTask(new Task(1, "completed!", true));

        //unComplete the task
        Task unCompletedTask = new Task(1, "completed!", false);
        db.updateTask(unCompletedTask);

        //get the updated task and compare them
        Task updatedTask = db.getTask(1);
        assertEquals(unCompletedTask.toString(), updatedTask.toString() );

    }

    /**
     * Tests uncompleting a task with multiple log entries.
     * 
     * updateTask() with complete = false should delete all entries from the system in the given time
     * 
     */
    @Test
    public void testTaskUpdateUncompleteWithAddedLogs(){

        long startTime = 1;
        long endTime   = 2;
        int  id        = 1; //the id of the task to uncomplete

      
        JDBCDAOimpl db = new JDBCDAOimpl(getTestDBURL(),startTime,endTime);
        fillDBWithTasks(db);
        fillDBWithEntries(db, startTime);
        fillDBWithEntries(db, endTime);

        //unComplete the task
        Task unCompletedTask = new Task(1, "completed!", false);
        db.updateTask(unCompletedTask);

        //get the updated task and compare them
        Task updatedTask = db.getTask(id);
        assertEquals(unCompletedTask.toString(), updatedTask.toString() );

        //assert that the only entries remaining are outside of the deleted log range
        assertEquals(2, db.findEntries(id).size());

        compareEntries(new ArrayList<Entry>(), db.findEntries(id, startTime,endTime));

    }
/**
 * 
 * tests the addLogEntry() method and the findEntries() method
 * 
 */
    @Test
    public void testEntryCreationAndFind(){

        long time = 1;

        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url);

        fillDBWithTasks(db);
        fillDBWithEntries(db,time);

        compareEntries(getExpectedEntries(time), db.findEntries());

    }

    @Test
    public void testEntryFindWithTasks(){

        long time = 1;

        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url);

        fillDBWithTasks(db);
        fillDBWithEntries(db,time);


        List<Task> tasks = db.getTasks();

        for(int i = 0; i < tasks.size(); i++){

           List<Entry> entries = db.findEntries(tasks.get(i).getTaskID());
           List<Entry> expectedEntries = getExpectedEntries(time);
      
            assertNotNull(entries);
            assertEquals(2, entries.size());

            assertEquals(entries.get(0).toString(), expectedEntries.get(2*i    ).toString());
            assertEquals(entries.get(1).toString(), expectedEntries.get(2*i + 1).toString());

        }
        
    }

    /**
     * Tests findEntry with different timestamps
     * 
     * 
     */
    @Test
    public void testEntryFindWithTime(){

        long time       = 3 ;

        

        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url);

        fillDBWithTasks     (db          );
        fillDBWithEntries   (db, time    );
        fillDBWithEntries   (db, time + 1);
        fillDBWithEntries   (db, time - 1);
    
       
       
     
        compareEntries( getExpectedEntries(time) ,db.findEntries(time      , time+1      )  );     
 
        

    }

    @Test
    public void testEntryUpdate(){

        JDBCDAOimpl db = new JDBCDAOimpl(getTestDBURL());

        fillDBWithTasks(db);
        fillDBWithEntries(db, 1);
        Entry entry = getExpectedEntries(1).get(0);

        entry.setContent("Updated!");
        entry.setTaskID(2);
        entry.setLogTime(2);

        db.updateEntry(entry);

        assertEquals(entry.toString(), db.getEntry(entry.getEntryID()).toString());


    }

    @Test
    public void testEntryRemoval(){

        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url);

        fillDBWithTasks(db);
        fillDBWithEntries(db, 1);

        db.deleteEntry(1);

        List<Entry> entries = db.findEntries(1);

        assertEquals(1, entries.size());
        assertNull(db.getEntry(1));

        


    }

    @Test
    public void testDeleteCascade(){

        JDBCDAOimpl db = new JDBCDAOimpl(getTestDBURL(), 0, 2);

        fillDBWithTasks(db);
        fillDBWithEntries(db, 1);


        //delete the task, and all the entries associated with it
        db.deleteTask(1);

        List<Entry> entries = db.findEntries(1);

        assertNull(db.getTask(1));
        assertEquals(0, entries.size());

    }

}