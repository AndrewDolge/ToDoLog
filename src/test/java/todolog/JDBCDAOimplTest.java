package todolog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

/**
 * Testing class for the JDBCDAOimpl class.
 * 
 * 
 */
public class JDBCDAOimplTest{


    public static String getTestDBURL(){
        return "jdbc:sqlite:test.db";
        //return "jdbc:sqlite::resource:test.db";
    }

    public static void fillDBWithEntries(JDBCDAOimpl db, long time){
        for(Entry e: getExpectedEntries(time)){
            db.addLogEntry(e.getTaskID(), e.getContent(), e.getlogTime());
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
        List<Entry> expected = new ArrayList<Entry>();
        expected.add(new Entry(1, 1, "1", logTime));
        expected.add(new Entry(2, 1, "2", logTime));
        expected.add(new Entry(3, 2, "3", logTime));
        expected.add(new Entry(4, 2, "4", logTime));
        expected.add(new Entry(5, 3, "5", logTime));
        expected.add(new Entry(6, 3, "6", logTime));
        return expected;
    }

    @After
    public void clearDB(){


        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try(Connection conn = DriverManager.getConnection(getTestDBURL())){
           

          conn.createStatement().executeUpdate("DROP TABLE IF EXISTS Tasks;");
          conn.createStatement().executeUpdate("DROP TABLE IF EXISTS Entries;");

        }catch(SQLException sqle){
            throw new RuntimeException("JDBCDAOimplTest.clearMemoryDB: SQL exception caught. error: " + sqle.getMessage(), sqle);
        }



    } 

   
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

        for(Task t: getExpectedTasks()){
            db.addTask(t.getName());
        }

        List<Task> tasks = db.getTasks();

        assertNotNull(tasks);
        assertEquals(3, tasks.size());

        for(int i = 0; i < tasks.size(); i++){
            assertEquals(tasks.get(i).toString(), getExpectedTasks().get(i).toString());
        }
        

    }//testTaskCreation


    @Test
    public void testTaskRollover(){

        JDBCDAOimpl db = new JDBCDAOimpl(getTestDBURL(), 2, 3);

        fillDBWithTasks(db);
        fillDBWithEntries(db, 1);

        List<Task> tasks = db.getTasks();
        List<Task> expectedTasks = getExpectedTasks();

        for(int i = 0; i < expectedTasks.size(); i++){

            assertEquals(tasks.get(i).toString(), expectedTasks.get(i).toString());
        }
            
     

    
    }//testTaskRollover

    @Test
    public void testTaskCompletion(){

        JDBCDAOimpl db = new JDBCDAOimpl(getTestDBURL(), 1, 3);

        fillDBWithTasks(db);
        fillDBWithEntries(db, 2);

        List<Task> tasks = db.getTasks();
        List<Task> expectedTasks = getExpectedTasksCompleted();

        for(int i = 0; i < expectedTasks.size(); i++){

            assertEquals(tasks.get(i).toString(), expectedTasks.get(i).toString());
        }

    }//testTaskCompletion


    @Test
    public void testTaskRemoval(){

        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url);

        fillDBWithTasks(db);

        db.removeTask(3);

        List<Task> tasks = db.getTasks();

        assertNotNull(tasks);
        assertNotNull(db.getTask(3));
        assertEquals(2, tasks.size());

        for(int i = 0; i < tasks.size(); i++){
            assertEquals(tasks.get(i).toString(), getExpectedTasks().get(i).toString());
        }
        
    }//testTaskRemoval

    @Test
    public void testTaskDeletion(){

        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url);

        fillDBWithTasks(db);

        db.deleteTask(3);

        List<Task> tasks = db.getTasks();

        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertNull(db.getTask(3));

        for(int i = 0; i < tasks.size(); i++){
            assertEquals(tasks.get(i).toString(), getExpectedTasks().get(i).toString());
        }
        
    }//testTaskCreation


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
     */
    @Test
    public void testTaskUpdateUncompleteWithAddedLogs(){

        
        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url,0,2);
        fillDBWithTasks(db);
        fillDBWithEntries(db, 1);
        fillDBWithEntries(db, 2);

        //unComplete the task
        Task unCompletedTask = new Task(1, "completed!", false);
        db.updateTask(unCompletedTask);

        //get the updated task and compare them
        Task updatedTask = db.getTask(1);
        assertEquals(unCompletedTask.toString(), updatedTask.toString() );

        //assert that the only entries remaining are outside of the deleted log range
        assertEquals(2, db.findEntries(1).size());

        for(Entry e: db.findEntries(1)){

            assertNotEquals((long) 1, e.getlogTime());

        }

    }

    @Test
    public void testEntryCreation(){

        long time = 1;

        String url = getTestDBURL();
        JDBCDAOimpl db = new JDBCDAOimpl(url);

        fillDBWithTasks(db);
        fillDBWithEntries(db,time);

        List<Task> tasks = db.getTasks();

        int i = 0;

        for(Task t: tasks){

           List<Entry> entries = db.findEntries(t.getTaskID());
           List<Entry> expectedEntries = getExpectedEntries(time);
      
            assertNotNull(entries);
            assertEquals(2, entries.size());

            assertEquals(entries.get(0).toString(), expectedEntries.get(2*i    ).toString());
            assertEquals(entries.get(1).toString(), expectedEntries.get(2*i + 1).toString());
            i++;
        }
    }

    @Test
    public void testEntryUpdate(){

        JDBCDAOimpl db = new JDBCDAOimpl(getTestDBURL());

        fillDBWithTasks(db);
        fillDBWithEntries(db, 1);
        Entry entry = getExpectedEntries(1).get(0);

        entry.setContent("Updated!");
        entry.setTaskID(2);
        entry.setlogTime(2);

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