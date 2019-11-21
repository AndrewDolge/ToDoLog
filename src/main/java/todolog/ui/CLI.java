package todolog.ui;

import java.io.File;

import com.google.gson.Gson;

import javafx.application.Application;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import todolog.core.Entry;
import todolog.core.EntryDAO;
import todolog.core.JDBCDAOimpl;
import todolog.core.Task;
import todolog.core.TaskDAO;
import todolog.ui.GUI;

/**
 * Command line Interface for th Jar file using picocli.
 * 
 */


@Command(name = "todolog", mixinStandardHelpOptions = true, version = "todolog 0.0.1", description = "daily todo with log entries.")
public class CLI implements Runnable{

    @Option(names={"-db","--dbPath"}, paramLabel = "SQLITEDB",description = "the path to the database file", defaultValue = "tdl.db")
    private File dbPath;
   
    @Spec 
    private CommandSpec spec;

    private EntryDAO entry;
    private TaskDAO  task;

    private void updateDAO(){
        JDBCDAOimpl impl = new JDBCDAOimpl("jdbc:sqlite:" + dbPath.getPath());

        this.entry = impl;
        this.task  = impl;
    }


    @Command(name = "addTask", description = "Adds a new daily task to the system.")
    public void addTask (@Parameters(paramLabel = "<name of task>", index = "0" ) String taskName){
        updateDAO();

        task.addTask(taskName);

    }//addTask

    @Command(name = "completeTask", description = "Completes a daily task.")
    public void completeTask(
        @Option(names = {"-t", "--taskID"},   description = "the id of the task", required = true) int   taskID,
        @Option(names = {"-c", "--content"},  description = "the content of the log")              String content
    ){
        updateDAO();

      
        
        if(content != null){
            entry.addLogEntry(taskID, content);
        }else{

            Task completed = task.getTask(taskID);
            completed.setCompleted(true);
            task.updateTask( completed);
        }

    }//completeTask

    @Command(name = "getTask", description = "gets the JSON of a given task, or all tasks")
    public void getTask(
        @Option(names = {"-t", "--taskID"}, description = "the id of the task", defaultValue = "-1") int taskID
    ){

        updateDAO();

        //if all tasks
        if(taskID == -1){

            System.out.println(new Gson().toJson(task.getTasks()));

        //else one task
        }else{

            Task t = task.getTask(taskID);
            //if the task is found, print to screen. Else print an empty json object
            if(task.getTask(taskID) != null){
                System.out.println(t.toString());
            }else{
                System.out.println("{}");
            }
           
        }

    }

    @Command(name = "removeTask", description = "removes a daily task")
    public void removeTask(
        @Option(names = {"-t", "--taskID"}, description = "the id of the task", required = true) int taskID
    ){
        updateDAO();

        task.removeTask(taskID);

    }//removeTask

    @Command(name = "updateEntry", description = "updates the content of an entry")
    public void updateEntry(


    @Option(names = {"-e", "--entryID"}, description = "the id of the log entry", required = true)  int entryID,
    @Option(names = {"-c", "--content"},  description = "the content of the log", required = true)  String content

    ){

        updateDAO();

        Entry updated = entry.getEntry(entryID);
        updated.setContent(content);

        entry.updateEntry(updated);
    }//updateEntry

    @Command(name = "getEntry", description = "get an entry with the given id")
    public void getEntry(
        @Option(names = {"-e", "--entryID"}, description = "the id of the log entry", required = true)  int entryID
    ){
        updateDAO();

        Entry e = entry.getEntry(entryID);

        if(e != null){
            System.out.println(e.toString());
        }else{
            System.out.println("{}");
        }

     
    }//getEntry

    @Command(name = "getEntries", description = "get a group of entries")
    public void getEntries(
        @Option(names = {"-t", "--taskID"}, description = "the id of the task", defaultValue = "-1") int taskID,
        @Option(names = {"-Mnm", "--minimumTime"}, description = "the minimum unix time an entry could have" ) Long minimumTime,
        @Option(names = {"-Mxm", "--maximumTime"}, description = "the maximum unix time an entry could have" ) Long maximumTime
    ){

        updateDAO();
        System.out.println(new Gson().toJson(entry.findEntries(taskID, minimumTime, maximumTime)));

    }//getEntries

    @Command(name = "removeEntry", description = "removes the entry with given id")
    public void removeEntry(
        @Option(names = {"-e", "--entryID"}, description = "the id of the log entry", required = true)  int entryID
    ){
        updateDAO();
        entry.deleteEntry(entryID);

    }//removeEntry

    @Command(name = "reset", description = "resets the database")
    public void reset(){

        task.resetTasks();
        entry.resetEntries();
    }

    @Override
    public void run() {
        
        Application.launch(GUI.class, "");
        //throw new CommandLine.ParameterException(this.spec.commandLine(), "Missing required subcommand");
    }

    public static void main(String[] args){
        int exitCode = new CommandLine(new CLI()).execute(args);
        System.exit(exitCode);
    }

}