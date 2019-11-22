/**
 * Sample Skeleton for 'paneEntryR.fxml' Controller Class
 */

package todolog.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import todolog.core.Entry;
import todolog.core.EntryDAO;
import todolog.core.Task;
import todolog.core.TaskDAO;
import todolog.core.ToDoLogFactory;
import todolog.util.TimeUtil;


/**
 * Controller class for the paneEntry.fxml view.
 * contains the references and callback methods for the controls in the pane entry.
 * 
 * 
 * TODO: implement deleting Entries
 */
public class TabEntryController implements TaskEntryDAOListener {

    private TaskDAO  taskDAO;
    private EntryDAO entryDAO;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="tableViewEntries"
    private TableView<TableEntry> tableViewEntries; // Value injected by FXMLLoader

    @FXML // fx:id="datePickerBefore"
    private DatePicker datePickerBefore; // Value injected by FXMLLoader

    @FXML // fx:id="choiceBoxTaskPicker"
    private ChoiceBox<Task> choiceBoxTaskPicker; // Value injected by FXMLLoader

    @FXML // fx:id="tableColumnTime"
    private TableColumn<TableEntry, String> tableColumnTime; // Value injected by FXMLLoader

    @FXML // fx:id="tableColumnTask"
    private TableColumn<TableEntry, String> tableColumnTask; // Value injected by FXMLLoader

    @FXML
    private Button buttonUpdate;

    @FXML // fx:id="buttonSearch"
    private Button buttonSearch; // Value injected by FXMLLoader

    @FXML // fx:id="textAreaEntryDisplay"
    private TextArea textAreaEntryDisplay; // Value injected by FXMLLoader

    private final Task nullTask = new Task(-1, "None",false);

    private final ObservableList<TableEntry> tableEntries = FXCollections.observableArrayList();

    private final ChangeListener<? super Number> tableChangeListener = (obs, oldSelection, newSelection) ->{
        if(newSelection != null){
            onTableViewChange();
        }
    };
       

    @FXML // This method is called by the FXMLLoader when initialization is complete
    public void initialize() {
        assert tableViewEntries != null : "fx:id=\"tableViewEntries\" was not injected: check your FXML file 'paneEntryR.fxml'.";
        assert buttonUpdate != null : "fx:id=\"buttonUpdate\" was not injected: check your FXML file 'paneEntryR.fxml'.";
        assert datePickerBefore != null : "fx:id=\"datePickerBefore\" was not injected: check your FXML file 'paneEntryR.fxml'.";
        assert choiceBoxTaskPicker != null : "fx:id=\"choiceBoxTaskPicker\" was not injected: check your FXML file 'paneEntryR.fxml'.";
        assert buttonSearch != null : "fx:id=\"buttonSearch\" was not injected: check your FXML file 'paneEntryR.fxml'.";
        assert textAreaEntryDisplay != null : "fx:id=\"textAreaEntryDisplay\" was not injected: check your FXML file 'paneEntryR.fxml'.";


        //add the cell factories to use the string formatter for the date.
        tableColumnTime.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("logTime"));
        tableColumnTask.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("taskName"));

   
        tableViewEntries.getSelectionModel().selectedIndexProperty().addListener(tableChangeListener);



    }//initialize


    /**
     * Fires when the search button is clicked.
     * clears the textAreaContentDisplay and updates the table.
     * 
     */
    @FXML
    public void onSearchButtonClick(MouseEvent event){
        textAreaEntryDisplay.clear();
        updateTable();
    }

    /**
     * fires when the update button is clicked.
     * 
     * takes the updated content from the textAreaContentDisplay and updates the entry with the new content.
     * Also updates the table.
     * 
     * 
     */
    @FXML
    void onUpdateButtonClick(MouseEvent event) {

        TableEntry selected = tableViewEntries.getSelectionModel().getSelectedItem();

        if(selected != null){

            int id = selected.getID();
            Entry e = entryDAO.getEntry(id);
            e.setContent(textAreaEntryDisplay.getText());

            entryDAO.updateEntry(e);
        }

        updateTable();
    }

    /**
     * Update the textAreaContentDisplay when the table view changes
     */
    private void onTableViewChange() {

        TableEntry selectedEntry = tableViewEntries.getSelectionModel().getSelectedItem();

        if(selectedEntry != null){

            textAreaEntryDisplay.setEditable(true);
            textAreaEntryDisplay.setText(selectedEntry.getContent());
        }else{
            textAreaEntryDisplay.setEditable(false);
        }



    }//onTableViewChange


    @FXML
    private void onTableViewKeyPress(KeyEvent event) {

        if(event.getCode() == KeyCode.BACK_SPACE || event.getCode()  == KeyCode.DELETE){

            TableEntry e = tableViewEntries.getSelectionModel().getSelectedItem();

            if(e != null){
               entryDAO.deleteEntry(e.getID());
                updateTable();
            } 
        }
    }//onTableViewKeyPress


    /**
     * Updates the choice box based on the TaskDAO.
     * Removes all tasks, repopulates the choice box with tasks from TaskDAO, and selects the default task.
     * 
     */
    public void updateChoiceBox() {

        choiceBoxTaskPicker.getItems().clear();
        choiceBoxTaskPicker.getItems().add(nullTask);

        for(Task t: taskDAO.getTasks()){
            choiceBoxTaskPicker.getItems().add(t);
        }

        choiceBoxTaskPicker.getSelectionModel().select(nullTask);
        
    }//updateChoiceBox

    /**
     * Updates the table based on the choiceBoxTaskPicker, and the datePicker.
     * 
     */
    public void updateTable(){

        tableEntries.clear();

        Task selected = choiceBoxTaskPicker.getSelectionModel().getSelectedItem();
        // set the search parameters if they are selected, otherwise default to null;
        Long    minTime = (datePickerBefore.getValue()  != null)     ? datePickerBefore.getValue().atTime(0, 0, 0).toEpochSecond(TimeUtil.getDefaultOffset()) : null;
        Long    maxTime = (datePickerBefore.getValue()  != null)     ? datePickerBefore.getValue().plusDays(1).atTime(0, 0, 0).toEpochSecond(TimeUtil.getDefaultOffset()) : null;
        Integer taskID  = (selected != null && selected != nullTask) ? selected.getTaskID()                     : null;

        for(Entry e: entryDAO.findEntries(taskID, minTime, maxTime)){

            tableEntries.add(
                        new TableEntry( 
                                taskDAO.getTask(e.getTaskID()).getName(),
                                    e.getLogTime(),
                                    e.getContent(),
                                    e.getEntryID()
                                )
            );

        }//for

        tableViewEntries.setItems(tableEntries);

        textAreaEntryDisplay.setEditable(false);

    }//updateTable

    /**
     * Inner class that loads the data into the table.
     * Changes the epoch second logTime to a human readable date format.
     * 
     * TODO: remove this and replace with a cellFactory for the tableViewEntries with just objects of type Entry.
     * 
     */
    public class TableEntry{

        private StringProperty taskName;
        private StringProperty logTime;
        private int id;
        private String content;

        public TableEntry(String taskName, long logTime, String content, int id){

            this.taskName   = new SimpleStringProperty(taskName);
            this.logTime    = new SimpleStringProperty(TimeUtil.convertEpochToString(logTime)); 
            this.content    = content;
            this.id         = id;
        }

        public String getTaskName() {
            return taskName.get();
        }

        public String getLogTime() {
            return logTime.get();
        }

        public String getContent(){
            return this.content;
        }

        public int getID(){
            return this.id;
        }        
    }//private class

    @Override
    public void onTaskEntryChange(TaskDAO taskDAO, EntryDAO entryDAO) {

        this.taskDAO = taskDAO;
        this.entryDAO = entryDAO;

        updateChoiceBox();
        updateTable();

    }


}//class
