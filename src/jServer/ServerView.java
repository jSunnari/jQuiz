package jServer;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Created by Jonas on 2016-02-25.
 */

public class ServerView {
    private Stage window;
    private static TextArea textArea = new TextArea();

    public ServerView(Stage window) {
        this.window = window;
        window.setTitle("jQuiz - Server");

        BorderPane mainPane = new BorderPane();

        HBox hBox = new HBox();
        Text text = new Text("jQuiz - Server");
        text.setStyle("-fx-font-size: 30px;");
        hBox.getChildren().add(text);
        hBox.setAlignment(Pos.CENTER);

        textArea.setStyle("-fx-background-radius: 5px");
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setPrefWidth(560);
        textArea.setPrefHeight(530);

        mainPane.setPadding(new Insets(10,10,10,10));
        mainPane.setBottom(textArea);
        mainPane.setTop(hBox);

        //Set scene:
        Scene scene = new Scene(mainPane, 800, 600);
        scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Lato:400,700"); //getting a font from google.
        scene.getStylesheets().add("jQuizStylesheet.css"); //check the file jQuizStylesheet.css in /src.
        window.setScene(scene);
        window.show();
    }

    public static void appendText(String message){
        textArea.appendText(message + "\n");
    }

}
