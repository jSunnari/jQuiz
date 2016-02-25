package jServer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.stage.Stage;

public class Main extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        ServerView serverView = new ServerView(primaryStage);
        Server server = new Server();
        Thread starter = new Thread(server);
        starter.start();

        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
            Platform.exit();
        });


    }

    public static void main(String[] args) {Application.launch(args);
    }

}
