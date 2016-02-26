package jServer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Main-class for the Server.
 * Starts the view and sends the primaryStage to the constructor.
 * Starts a new thread of the "Server"-class
 */
public class ServerMain extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        ServerView serverView = new ServerView(primaryStage);
        Server server = new Server();
        Thread starter = new Thread(server);
        starter.start();

        /**
         * Shutdown-hook for the server-application.
         * Shuts down the application properly.
         */
        primaryStage.setOnCloseRequest(event -> {
            closeApp();
        });

        serverView.closeButtonListener(event -> {
            closeApp();
        });
    }
    public static void main(String[] args) {Application.launch(args);}

    void closeApp(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("jQuiz-Server");
        alert.setHeaderText("Disconnect - the server-application will now close");
        alert.setContentText("Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.exit(0);
            Platform.exit();
        }
    }
}
