package todolog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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


   @After
    public void clearDB(){


        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try(Connection conn = DriverManager.getConnection(getTestDBURL())){
           

/*           conn.createStatement().execute("PRAGMA foreign_keys = OFF;\n" +
                                        "DELETE FROM Tasks;" +
                                        "DELETE FROM Entries;"+
                                        "DROP TABLE IF EXISTS Entries;\n" +
                                        "DROP TABLE IF EXISTS Tasks;\n" +
                                        "PRAGMA foreign_keys = ON;\n"); */

          conn.createStatement().executeUpdate("DROP TABLE IF EXISTS Tasks;");
          conn.createStatement().executeUpdate("DROP TABLE IF EXISTS Entries;");



        }catch(SQLException sqle){
            throw new RuntimeException("JDBCDAOimplTest.clearMemoryDB: SQL exception caught. error: " + sqle.getMessage(), sqle);
        }



    } 

    public static List<Task> getExpectedTasks(){

        List<Task> expected = new ArrayList<Task>();
        expected.add(new Task(1,"laundry",false));
        expected.add(new Task(2,"sweeping",false));
        expected.add(new Task(3,"homework",false));  
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

    /**
     * Tests if the database can be created and set up correctly.
     * 
     * 
     */
    @Test
    public void TestDatabaseCreation(){

      
        JDBCDAOimpl db = new JDBCDAOimpl(getTestDBURL());

    }

    /**
     * Tests the creation of new tasks.
     * 
     */
    @Test
   
    public void TestDatabaseTaskCreation(){


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
        

    }//testDatabaseTaskCreation




}