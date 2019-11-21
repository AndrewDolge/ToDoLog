package todolog.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import todolog.core.EntryDAO;
import todolog.core.TaskDAO;

/**
 * Controller class for the main.fxml view.
 * 
 * Loads the main tabPane and has access to the other controllers.
 * 
 */
public class MainController implements Initializable, TaskEntryDAOListener {

    @FXML
    private TabPane tabPane;

    @FXML
    private TabTaskController paneTaskIncludeController;

    @FXML
    private TabEntryController paneEntryIncludeController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * Called when the entry tab is selected. updates the choice box in the entry
     * tab with the tasks that may have been modified in the task tab.
     * 
     */
    @FXML
    public void onTabEntrySelect() {

        paneEntryIncludeController.updateChoiceBox();
        paneEntryIncludeController.updateTable();

    }// onTabEntrySelect

    public void shutdown() {
        paneTaskIncludeController.onShutdown();

    }

    @Override
    public void onTaskEntryChange(TaskDAO taskDAO, EntryDAO entryDAO) {
        paneEntryIncludeController.onTaskEntryChange(taskDAO, entryDAO);
        paneTaskIncludeController.onTaskEntryChange(taskDAO, entryDAO);

    }



}//class
