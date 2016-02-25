package jServer;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        Server server = new Server();
        Thread starter = new Thread(server);
        starter.start();
    }

    public static void main(String[] args) {Application.launch(args);
    }
}
