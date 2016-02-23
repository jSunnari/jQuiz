package jClient;

import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * Created by Jonas on 2016-02-17.
 */

public class ClientController extends Application {
    ClientView view;
    ClientModel model;

    @Override
    public void start(Stage primaryStage) throws Exception {

        view = new ClientView(primaryStage);
        model = new ClientModel();

        view.connectButtonListener(event -> {

            if (view.emptyFields()) {


                model.setUserName(view.getUsername());
                model.connect(view.getPort(), view.getIp());

                //if the connection goes well:
                if (model.getConnected()) {

                    Thread clientThread = new Thread(model);
                    clientThread.start();

                    view.initConnected();
                }
            }

            //TEST



        });

        view.sendButtonListener(event -> sendButton());

        view.disconnectButtonListener(event -> {
            model.disconnect();
        });

        view.messageTextAreaListener(event -> {
            if (event.getCode() == KeyCode.ENTER){
                sendButton();
                event.consume();
            }
        });





        //Shutdown-hook, closes the application and socket properly when closing the window.
        if (model.getConnected()) {
            primaryStage.setOnCloseRequest(event -> {
                model.shutDownDisconnect();
            });
        }




    }



    void sendButton(){
        if (model.getConnected() && !view.getMessageTextArea().equals("")) {
            model.send(view.getMessageTextArea());
            view.setMessageTextArea();
        }
    }





    public static void main(String[] args) {
        Application.launch(args);
    }
}
