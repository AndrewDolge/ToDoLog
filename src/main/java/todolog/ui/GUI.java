package todolog.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import todolog.core.EntryDAO;
import todolog.core.TaskDAO;
import todolog.core.ToDoLogFactory;


/**
 * 
 * Subclass of application for JavaFX GUI.
 * Loads the main fxml file, sets the scene, and shows the application.
 */
public class GUI extends Application {

    private MainController mainController;
    private Parent root;

    private EntryDAO entryDAO;
    private TaskDAO  taskDAO;

    @Override
    public void start(Stage stage) throws Exception {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));

            root                  = loader.load();
            mainController = loader.getController();

            updateDAO();
            Scene scene = new Scene(root, 545, 405);
            scene.getStylesheets().add("tabTask.css");

            stage.setResizable(false);
            stage.setTitle("To Do Log");
            stage.setScene(scene);

            stage.onCloseRequestProperty().addListener( (l)->mainController.shutdown());

            stage.show();

        } catch (Exception ioe) {

            ioe.printStackTrace();
            // throw new RuntimeException(ioe.getMessage());
            System.exit(-1);
        }

    }

    /**
     * Refreshes the DAO object models, and calls the TaskEntryDAOListeners in the controllers.
     * 
     * 
     */
    public void updateDAO(){

        ToDoLogFactory factory = new ToDoLogFactory("jdbc:sqlite:" + "tdl.db");

        this.entryDAO = factory.getEntryDAO();
        this.taskDAO  = factory.getTaskDAO();

        mainController.onTaskEntryChange(taskDAO, entryDAO);
    }

    @Override
    public void stop() throws Exception{
        mainController.shutdown();

    }
    

    public static void main(String[] args) {
        launch(args);
    }

}// class