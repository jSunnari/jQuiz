package jClient;

import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * The controller for the client. - MainClass.
 * Communicates between the GUI and the Model.
 * Includes listeners and implements the interface "MessageInterface".
 */

/**
 * Created by Jonas on 2016-02-17.
 */

public class ClientController extends Application implements MessageInterface {
    ClientView view;
    ClientModel model;

    @Override
    public void start(Stage primaryStage) throws Exception {

        /**
         * References to the view and the model, the view gets the primarystage in the constructor and the model gets
         * "this" which in this case references to the interface "MessageInterface".
         */
        view = new ClientView(primaryStage);
        model = new ClientModel(this);

        /**
         * CONNECT-BUTTON.
         * If any of the textfields are empty, they will be marked red.
         * Else, the client will try to connect.
         * If the connection goes well, the thread will start listening for messages and the GUI-window will initialize.
         */
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
        });

        /**
         * DISCONNECT-BUTTON.
         * Calls the disconnect-method in the model-class.
         */
        view.disconnectButtonListener(event -> {
            model.disconnect();
        });

        /**
         * You can use the Enter-button instead of clicking the send-button when sending messages.
         */
        view.messageTextAreaListener(event -> {
            if (event.getCode() == KeyCode.ENTER){
                sendButton();
                event.consume();
            }
        });
        /**
         * But you can still use the send-button :)
         */
        view.sendButtonListener(event -> sendButton());

        /**
         * Shutdown-hook, closes the application and socket properly when closing the window. (if connected).
         */
        if (model.getConnected()) {
            primaryStage.setOnCloseRequest(event -> {
                model.shutDownDisconnect();
            });
        }
    }

    /**
     * Method for sending a message. (if connected and if there is something to send).
     */
    void sendButton(){
        if (model.getConnected() && !view.getMessageTextArea().equals("")) {
            model.send(view.getMessageTextArea());
            view.clearMessageTextArea();
        }
    }

    /**
     * Main method with all the implemented methods from the interface MessageInterface.
     * Appends the TextFlow in the viewclass (in different formats) with the the message-text from the modelclass.
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void appendOrangeBold() {
        view.appendOrangeBold(model.getMessage());
    }

    @Override
    public void appendRed() {
        view.appendRed(model.getMessage());
    }

    @Override
    public void appendGreen() {
        view.appendGreen(model.getMessage());
    }

    @Override
    public void appendBlue() {
        view.appendBlue(model.getMessage());
    }

    @Override
    public void appendPurpleBold() {
        view.appendPurpleBold(model.getMessage());
    }

    @Override
    public void appendRegular() {
        view.appendRegular(model.getMessage());
    }

    @Override
    public void sendUserList() {
        view.setUserList(model.getCurrentUsers());
    }

    @Override
    public void changeUserName() {
        view.setUserName(model.getUserName());
    }
}
