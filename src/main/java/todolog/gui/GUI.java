package todolog.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * 
 * Subclass of application for JavaFX GUI.
 * Loads the main fxml file, sets the scene, and shows the application.
 */
public class GUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        try {

            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));

            Scene scene = new Scene(root, 545, 405);

            scene.getStylesheets().add("tabTask.css");

            stage.setResizable(false);
            stage.setTitle("To Do Log");
            stage.setScene(scene);
            stage.show();

        } catch (Exception ioe) {

            ioe.printStackTrace();
            // throw new RuntimeException(ioe.getMessage());
            System.exit(-1);
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}// class