package jClient;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * Created by Jonas on 2016-02-17.
 */
public class ClientView {

    private Stage window;
    private BorderPane mainPane;
    private GridPane connectPane;
    private Label ipLabel = new Label("Ip-number:");
    private Label portLabel = new Label("Port:");
    private Label usernameLabel = new Label("Username:");
    private TextField ipTextfield = new TextField("127.0.0.1");
    private TextField portTextfield = new TextField("50123");
    private TextField usernameTextfield = new TextField();
    private Button disconnectButton = new Button("Disconnect");
    private Button connectButton = new Button("Connect");
    private Pane spring1 = new Pane();
    private Pane spring2 = new Pane();
    static ListView<String> userList = new ListView<>();
    private Label userlistLabel = new Label("Userlist:");
    private Label chatTextLabel = new Label("Chat:");
    static TextArea chatTextArea = new TextArea();
    private TextArea messageTextArea = new TextArea();
    private Button sendButton = new Button("Send");


    private ScrollPane textContainer = new ScrollPane();
    private static TextFlow textFlowArea = new TextFlow();


    public ClientView(Stage window) {
        //init window:
        this.window = window;
        window.setTitle("jQuiz");




        textFlowArea.getChildren().addListener(
                (ListChangeListener<Node>) ((change) -> {
                    textFlowArea.layout();
                    textContainer.layout();
                    textContainer.setVvalue(1.0f);
                    textContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                }));
        textContainer.setContent(textFlowArea);

        usernameTextfield.setOnMouseClicked(event -> usernameTextfield.setStyle("-fx-control-inner-background: white"));
        ipTextfield.setOnMouseClicked(event -> ipTextfield.setStyle("-fx-control-inner-background: white"));
        portTextfield.setOnMouseClicked(event -> portTextfield.setStyle("-fx-control-inner-background: white"));




        //build window:
        buildWindow();





        //set scene:
        Scene scene = new Scene(mainPane, 800, 600);
        scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Lato:400,700");
        scene.getStylesheets().add("jQuizStylesheet.css");
        window.setScene(scene);
        window.show();
    }

    void buildWindow(){

        //UPPER PANEL, CONNECTION-INFORMATION:
        connectPane = new GridPane();
        connectPane.setPadding(new Insets(10,10,10,10));
        connectPane.setVgap(8);
        connectPane.setHgap(10);

        connectButton.setMinWidth(85);
        ipTextfield.setMaxWidth(120);
        ipTextfield.setPromptText("127.0.0.1");
        portTextfield.setMaxWidth(60);
        portTextfield.setPromptText("50123");
        usernameTextfield.setMaxWidth(120);
        spring1.setMinWidth(10);
        spring2.setMinWidth(50);

        GridPane.setConstraints(usernameLabel,0,0);
        GridPane.setConstraints(usernameTextfield,1,0);
        GridPane.setConstraints(spring1,2,0);
        GridPane.setConstraints(ipLabel,3,0);
        GridPane.setConstraints(ipTextfield,4,0);
        GridPane.setConstraints(portLabel,5,0);
        GridPane.setConstraints(portTextfield,6,0);
        GridPane.setConstraints(spring2,7,0);
        GridPane.setConstraints(connectButton,8,0);
        GridPane.setConstraints(disconnectButton,9,0);

        connectPane.getChildren().addAll(ipLabel,usernameTextfield,portLabel,ipTextfield,spring1,usernameLabel,portTextfield,spring2,connectButton,disconnectButton);

        //RIGHT PANEL, ONLINE USERS:
        VBox userlistBox = new VBox(5);
        userlistBox.setPadding(new Insets(10,10,10,10));
        userlistBox.setAlignment(Pos.CENTER);
        userlistBox.setMaxWidth(205);
        userlistBox.getChildren().addAll(userlistLabel,userList);

        //LEFT PANEL, MAIN CHAT TEXTAREA:
        VBox chatTextBox = new VBox(5);
        chatTextBox.setPadding(new Insets(10,10,10,10));
        chatTextBox.setAlignment(Pos.CENTER);
        chatTextBox.getChildren().addAll(chatTextLabel, textContainer);
        textFlowArea.setStyle("-fx-background-color: white; -fx-font-size: 14px;");
        textFlowArea.setPrefHeight(400);
        textFlowArea.setPrefWidth(560);



        //BOTTOM PANEL, MESSAGE AREA:
        HBox messageBox = new HBox(10);
        messageBox.setPadding(new Insets(10,10,10,10));
        messageBox.getChildren().addAll(messageTextArea,sendButton);
        messageTextArea.setPrefHeight(60);
        messageTextArea.setPrefWidth(506);
        messageTextArea.setWrapText(true);
        messageTextArea.setStyle("-fx-background-radius: 5px");
        sendButton.setPrefHeight(60);

        //INIT MAIN PANEL:
        mainPane = new BorderPane();
        mainPane.setTop(connectPane);
        mainPane.setRight(userlistBox);
        mainPane.setLeft(chatTextBox);
        mainPane.setBottom(messageBox);

    }

    public static void appendOrangeBold(String msg){
        append(msg, "-fx-font-weight: 700; -fx-fill: #d58300;");
    }

    public static void appendRed(String msg){
        append(msg, "-fx-fill: red");
    }

    public static void appendGreen(String msg){
        append(msg, "-fx-fill: green");
    }

    public static void appendBlue(String msg){
        append(msg, "-fx-fill: blue");
    }

    public static void appendPurpleBold(String msg){
        append(msg, "-fx-fill: purple;");
    }

    public static void appendRegular(String msg){
        append(msg, "");
    }

    private static synchronized void append(String msg, String style) {
        Platform.runLater(() -> {
            Text t = new Text(msg);
            //t.setFont();
            if (!style.equals("")) {
                t.setStyle(style);
            }
            textFlowArea.getChildren().add(t);
        });
    }



    public String getIp(){
        return ipTextfield.getText();
    }

    public int getPort(){
        return Integer.parseInt(portTextfield.getText());
    }

    public String getUsername(){
        return usernameTextfield.getText();
    }

    public void appendChatTextArea(String text){
        chatTextArea.appendText(text);
    }

    public String getMessageTextArea(){
        return messageTextArea.getText();
    }

    public void setMessageTextArea(){
        messageTextArea.clear();
    }

    public void setUserList(ObservableList<String> userObservableList){
        userList.setItems(userObservableList);
    }

    public void setUserListView(ListView<String> listView){
        userList = listView;
    }

    public void initConnected(){
        connectButton.setDisable(true);
        usernameTextfield.setDisable(true);
        ipTextfield.setDisable(true);
        portTextfield.setDisable(true);

    }

    public boolean emptyFields(){
        boolean readyToConnect;

        String backgroundColor = "-fx-control-inner-background: red";

        if (usernameTextfield.getText().equals("")){
            usernameTextfield.setStyle(backgroundColor);
            readyToConnect = false;
        }

        else if (ipTextfield.getText().equals("")){
            ipTextfield.setStyle(backgroundColor);
            readyToConnect = false;
        }

        else if (portTextfield.getText().equals("")){
            portTextfield.setStyle(backgroundColor);
            readyToConnect = false;
        }

        else {
            readyToConnect = true;
        }

        return readyToConnect;

    }



    void connectButtonListener (EventHandler<ActionEvent> clickButton){
        connectButton.setOnAction(clickButton);
    }

    void sendButtonListener (EventHandler<ActionEvent> clickButton){
        sendButton.setOnAction(clickButton);
    }

    void disconnectButtonListener (EventHandler<ActionEvent> clickButton){
        disconnectButton.setOnAction(clickButton);
    }

    void messageTextAreaListener (EventHandler<KeyEvent> keyListener){
        messageTextArea.setOnKeyPressed(keyListener);


    }
}
