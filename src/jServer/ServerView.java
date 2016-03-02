package jServer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * A simple GUI for the server.
 * Shows the log.
 */

/**
 * Created by Jonas on 2016-02-25.
 */

public class ServerView {
    private Stage window;
    private static TextArea textArea = new TextArea();
    private Button closeButton;

    public ServerView(Stage window) {
        //init window.
        this.window = window;
        window.setTitle("jQuiz - Server");

        //main borderpane:
        BorderPane mainPane = new BorderPane();

        //hbox as "header" and close-button.
        HBox hBox = new HBox(407);
        Text headertext = new Text("jQuiz - Server");
        closeButton = new Button("Close server");
        closeButton.setPrefHeight(35);
        headertext.setStyle("-fx-font-size: 30px;");
        hBox.getChildren().addAll(headertext,closeButton);

        //text-area for showing the log.
        textArea.setStyle("-fx-background-radius: 5px");
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setPrefWidth(460);
        textArea.setPrefHeight(450);

        //init mainpane:
        mainPane.setPadding(new Insets(10,10,10,10));
        mainPane.setBottom(textArea);
        mainPane.setTop(hBox);

        //set scene:
        Scene scene = new Scene(mainPane, 700, 520);
        scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Lato:400,700"); //getting a font from google.
        scene.getStylesheets().add("jQuizStylesheet.css"); //check the file jQuizStylesheet.css in /src.
        window.setScene(scene);
        window.show();
    }

    /**
     * Method that appends the textarea with messages sent from the Server-classes.
     * @param message = incomming message from other classes.
     */
    public static void appendText(String message){
        if (message.startsWith("STRGAME") || message.startsWith("ENDGAME")){
            message = message.substring(7);
        }
        else if ( message.startsWith("QUESTION")){
            message = message.substring(8);
        }
        textArea.appendText(message + "\n");
    }

    void closeButtonListener (EventHandler<ActionEvent> closeButtonListener){
        closeButton.setOnAction(closeButtonListener);
    }
}
