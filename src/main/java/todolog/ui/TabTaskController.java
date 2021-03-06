
package todolog.ui;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import todolog.core.Entry;
import todolog.core.EntryDAO;
import todolog.core.Task;
import todolog.core.TaskDAO;
import todolog.util.TempEntrySerializer;
import todolog.util.TimeUtil;

public class TabTaskController implements TaskEntryDAOListener {

    private static Logger log = LogManager.getLogger(TabTaskController.class.getName());

    @FXML
    private TabPane tabPane;

    @FXML
    private Button buttonAdd;
    @FXML
    private Button buttonRecord;

    @FXML
    private TextArea textAreaContent;
    @FXML
    private TextField textFieldTaskName;

    @FXML
    private ListView<Task> listViewTasks;

    private TaskDAO taskDAO;
    private EntryDAO entryDAO;

    private TempEntrySerializer tempEntries;

    private ChangeListener<Task> changeListener = new ChangeListener<Task>() {
        @Override
        public void changed(ObservableValue<? extends Task> observable, Task oldValue, Task newValue) {
            onListViewChange(oldValue, newValue);
        }
    };

    @FXML
    public void initialize() {

        assert listViewTasks != null : "fx:id=\"listViewTasks\" was not injected: check your FXML file 'paneTaskR.fxml'.";
        assert textAreaContent != null : "fx:id=\"textAreaContent\" was not injected: check your FXML file 'paneTaskR.fxml'.";
        assert buttonRecord != null : "fx:id=\"buttonRecord\" was not injected: check your FXML file 'paneTaskR.fxml'.";
        assert buttonAdd != null : "fx:id=\"buttonAdd\" was not injected: check your FXML file 'paneTaskR.fxml'.";
        assert textFieldTaskName != null : "fx:id=\"textFieldTaskName\" was not injected: check your FXML file 'paneTaskR.fxml'.";

        // initialize the temporary entries
        tempEntries = new TempEntrySerializer();
        tempEntries.readFromFile("temp.json");

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

    }//initialize


     public void onShutdown(){

        tempEntries.writeToFile("temp.json");

     }//onShutdown


    /**
     * When the selected task in the listView is changed, load the content from temporary storage into 
     * the textAreaContent
     * 
     * 
     */
    private void onListViewChange(Task oldTask, Task selectedTask) {

        textAreaContent.setEditable(false);

        if(selectedTask != null){
            loadContent(selectedTask.getTaskID());
            textAreaContent.setEditable(true);

        }

      
    }//onListViewChange

    @FXML 
    public void onTextAreaTyped(){

            if( getSelectedTask() != null){
                saveContent(getSelectedTask().getTaskID(), textAreaContent.getText());
            }
            
    }//onTextAreaTyped

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

            recordContent(task.getTaskID(), textAreaContent.getText());
    
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

        textAreaContent.setEditable(false);
        textAreaContent.clear();

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
     * gets the selected task
    */
    private Task getSelectedTask(){
        return listViewTasks.getSelectionModel().getSelectedItem();
    }//getSelectedTask



    /**
     * Saves the text of the textAreaContent into the temporary content map
     * @param id the id to store in the map
     * @param content the string to save
     */
    private void saveContent(Integer id, String content){
        if(isValidString(content)){
            tempEntries.put(id, content, TimeUtil.getNow());
        }//if

    }//saveContent

    /**
     * Updates the view such that the textAreaContent element loads the text saved with the given id
     * @param id the id of the task with the temporary content
     * 
     */
    private void loadContent(Integer id){

        String content = "";

        //if the entry is saved in temporary storage
        if(tempEntries.exists(id)){
            content = tempEntries.getContent(id);
        }else{

            List<Entry> entries = entryDAO.findEntries(id,TimeUtil.getDayStart(),null);

            if(!entries.isEmpty()){
                entries.sort((a,b) -> { return (int) (a.getLogTime() - b.getLogTime());    }  );
                content = entries.get(0).getContent();
            }
        }

        textAreaContent.setText(content);
    }//loadContent

    /**
     * Records the content into the System, removing it from temporary storage.
     * 
     */
    private void recordContent(Integer taskID, String content, Long logTime){

        if(logTime == null){
            entryDAO.addLogEntry(taskID, content);
        }else{
            entryDAO.addLogEntry(taskID, content,logTime);
        }

        if(tempEntries.exists(taskID)){
            tempEntries.remove(taskID);
        }
       
    }

    private void recordContent(Integer taskID, String content){
        recordContent(taskID, content,null);
    }



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

    @Override
    public void onTaskEntryChange(TaskDAO taskDAO, EntryDAO entryDAO) {

        if(this.taskDAO != null && this.entryDAO != null){

            for(Integer id : tempEntries.getIDs()){

                //if the temp entry was made after yesterday, and it hasn't been completed
                if(
                    tempEntries.getLogTime(id) < TimeUtil.getDayStart() &&
                    !(
    
                        this.taskDAO.getTask(id) != null                  &&
                        this.taskDAO.getTask(id).isCompleted()
                    ) 
                ){

                    recordContent(id, tempEntries.getContent(id), tempEntries.getLogTime(id));
        
                }//if
    
            }//for every entry in temp content

        }//if the task and entry dao are not null

        this.taskDAO  = taskDAO;
        this.entryDAO = entryDAO;

        updateListView();

    }
    

}//class