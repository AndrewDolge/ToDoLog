
package todolog.gui;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import todolog.EntryDAO;
import todolog.Task;
import todolog.TaskDAO;
import todolog.TimeUtil;
import todolog.ToDoLogFactory;

public class TabTaskController implements Initializable {

    @FXML private TabPane  tabPane;

    @FXML private Button buttonAdd;
    @FXML private Button buttonRecord;

    @FXML private TextArea textAreaContent;
    @FXML private TextField textFieldTaskName;
  
    @FXML private ListView<Task> listViewTasks;

  
    private TaskDAO  taskDAO;
    private EntryDAO entryDAO;

    private HashMap<Integer,String> tempContent;
    private HashMap<Integer,Long>   tempTimestamp;

    private  ChangeListener<Task> changeListener = new ChangeListener<Task>()
    {
        @Override
        public void changed(ObservableValue<? extends Task> observable, Task oldValue,Task newValue) {
            onListViewChange(oldValue,newValue);
        }
    };

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        assert listViewTasks != null : "fx:id=\"listViewTasks\" was not injected: check your FXML file 'paneTaskR.fxml'.";
        assert textAreaContent != null : "fx:id=\"textAreaContent\" was not injected: check your FXML file 'paneTaskR.fxml'.";
        assert buttonRecord != null : "fx:id=\"buttonRecord\" was not injected: check your FXML file 'paneTaskR.fxml'.";
        assert buttonAdd != null : "fx:id=\"buttonAdd\" was not injected: check your FXML file 'paneTaskR.fxml'.";
        assert textFieldTaskName != null : "fx:id=\"textFieldTaskName\" was not injected: check your FXML file 'paneTaskR.fxml'.";

        //initialize the model
        tempContent   = new HashMap<Integer,String>();
        tempTimestamp = new HashMap<Integer,Long>();

        ToDoLogFactory factory = new ToDoLogFactory("jdbc:sqlite:" + "tdl.db");

        taskDAO = factory.getTaskDAO();
        entryDAO = factory.getEntryDAO();

        //initialize the view
        textAreaContent.setEditable(false);

        listViewTasks.setCellFactory(new Callback<ListView<Task>, ListCell<Task>>(){

			@Override
			public ListCell<Task> call(ListView<Task> param) {
				return new TaskCell();
			}
        });


        //add a listener to the ListView to trigger on a task change
        listViewTasks.getSelectionModel().selectedItemProperty().addListener(changeListener);

        //update the task list
        updateListView();
    }//initialize

    /**
     * When the selected task in the listView is changed, load the content from temporary storage into 
     * the textAreaContent
     * 
     * 
     */
    private void onListViewChange(Task oldTask, Task selectedTask) {

        textAreaContent.setEditable(false);

        loadContent(selectedTask.getTaskID());

        textAreaContent.setEditable(true);
    }//onListViewChange

    @FXML 
    public void onTextAreaTyped(){

            if( getSelectedTask() != null){
                saveContent(getSelectedTask().getTaskID(), textAreaContent.getText());
            }
            
    }


    /**
     * When the add button is clicked, create a new task using the name of the textFieldTaskName component.
     * Updates the list view.
     * 
     */
    @FXML
    public void onAddButtonClick(MouseEvent event) {
        buttonAdd.setDisable(true);

        if( isValidString(textFieldTaskName.getText())){
            taskDAO.addTask(textFieldTaskName.getText());
            updateListView();
        }//if

        buttonAdd.setDisable(false);
    }//onAddButtonClick

    /**
     * When the record button is clicked, create a new log entry with the selected task and the 
     * text in the textAreaContent control.
     * 
     * updates the list view.
     * 
     */
    @FXML
    public void onRecordButtonClick(MouseEvent event){

    buttonRecord.setDisable(true);
        Task task =  listViewTasks.getSelectionModel().getSelectedItem();

        if(isValidString(textAreaContent.getText())){

            entryDAO.addLogEntry(task.getTaskID(), textAreaContent.getText());
        }

        //update the list view
        updateListView();
        //reselect the task
        listViewTasks.getSelectionModel().select(task);
        buttonRecord.setDisable(false);
    
    }//onRecordButtonClick


    /**
     * When the delete key or backspace key is pressed when the list is focused, delete the selected entry
     * 
     */
    @FXML
    void onListViewKeyPress(KeyEvent event) {

        if(event.getCode() == KeyCode.BACK_SPACE || event.getCode()  == KeyCode.DELETE){

            Task t = listViewTasks.getSelectionModel().getSelectedItem();

            if(t != null){
                taskDAO.removeTask(t.getTaskID());
                updateListView();
            } 
        }

    }//onListViewKeyPress

    /**
     * Removes all tasks from the list view, and updates the list view from the system.
     * 
     */
    public void updateListView(){

        ObservableList<Task> tasks = listViewTasks.getItems();
        
        //remove the listener while modifying the list
        listViewTasks.getSelectionModel().selectedItemProperty().removeListener(changeListener);

        //remove all tasks from the list
        while(!tasks.isEmpty()){
            tasks.remove(0);
        }

        tasks.addAll(taskDAO.getTasks());

        //add a listener to the ListView to trigger on a task change
        listViewTasks.getSelectionModel().selectedItemProperty().addListener( changeListener);
    }//updateListView

    /**
     * Determine if a string is valid
     * @param str the string to validate
     * @return True, if the string is not null and is not blank. False otherwise.
     */
    private boolean isValidString(String str){
        
        return str !=null && !str.isBlank();
    }//isValidString

    /**
     * Saves the text of the textAreaContent into the temporary content map
     * @param id the id to store in the map
     * @param content the string to save
     */
    private void saveContent(Integer id, String content){
        if(isValidString(content)){
            tempContent.put  (id, content);
            tempTimestamp.put(id, TimeUtil.getNow());
        }//if

    }//saveContent

    /**
     * Updates the view such that the textAreaContent element loads the text saved with the given id
     * @param id the id of the task with the temporary content
     * 
     */
    private void loadContent(Integer id){
        textAreaContent.setText( tempContent.getOrDefault(id, ""));
    }//loadContent

    private Task getSelectedTask(){

        return listViewTasks.getSelectionModel().getSelectedItem();
    }//getSelectedTask


    private final class TaskCell extends ListCell<Task>{

        @Override
        protected void updateItem(Task t, boolean empty){
            super.updateItem(t,empty);

            if(t != null){
                setText(t.getName());

                if(t.isCompleted()){
                    getStyleClass().add("task-completed");
                }else{
                    getStyleClass().remove("task-completed");
                }

            }else{
                setText("");
                getStyleClass().remove("task-completed");
            }
        }

    }
    

}//class