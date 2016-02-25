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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * The GUI for the client.
 * Main parts:
 * Upper window = Connection part.
 * Left window = TextFlow for main chat-window (TextFlow because it can do different fonts and colors etc.)
 *               TextArea for writing messages.
 * Right window = Online users ListView.
 */

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
    private ListView<String> userList = new ListView<>();
    private Label userlistLabel = new Label("Userlist:");
    private Label chatTextLabel = new Label("Chat:");
    private TextArea messageTextArea = new TextArea();
    private Button sendButton = new Button("Send");
    private ScrollPane textContainer = new ScrollPane();
    private TextFlow textFlowArea = new TextFlow();

    public ClientView(Stage window) {
        //Init window:
        this.window = window;
        window.setTitle("jQuiz");

        //Build window and add listeners for GUI-changes:
        buildWindow();
        guiListeners();

        //Set scene:
        Scene scene = new Scene(mainPane, 800, 600);
        scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Lato:400,700"); //getting a font from google.
        scene.getStylesheets().add("jQuizStylesheet.css"); //check the file jQuizStylesheet.css in /src.
        window.setScene(scene);
        window.show();
    }

    /**
     * Method that builds the GUI.
     */
    void buildWindow(){
        //UPPER PANEL, CONNECTION-INFORMATION: -------------------------------------------------------------------------
        connectPane = new GridPane();
        connectPane.setPadding(new Insets(10,10,10,10));
        connectPane.setVgap(8);
        connectPane.setHgap(10);

        connectButton.setMinWidth(80);
        ipTextfield.setMaxWidth(120);
        ipTextfield.setPromptText("127.0.0.1");
        portTextfield.setMaxWidth(60);
        portTextfield.setPromptText("50123");
        usernameTextfield.setMaxWidth(120);
        spring1.setMinWidth(10);
        spring2.setMinWidth(55);
        chatTextLabel.setFont(Font.font(16));
        userlistLabel.setFont(Font.font(16));

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
        connectPane.setStyle("-fx-background-color: rgba(158, 136, 157, 0.76); -fx-background-radius: 0 0 20 20;");

        //RIGHT PANEL, ONLINE USERS: -----------------------------------------------------------------------------------
        VBox userlistBox = new VBox(5);
        userlistBox.setPadding(new Insets(10,10,25,0));
        userlistBox.setAlignment(Pos.BOTTOM_CENTER);
        userlistBox.getChildren().addAll(userlistLabel,userList);
        userList.setPrefHeight(476);
        userList.setPrefWidth(170);

        //LEFT PANEL, MAIN CHAT TEXTAREA: ------------------------------------------------------------------------------
        VBox chatTextBox = new VBox(5);
        chatTextBox.setPadding(new Insets(10,0,10,10));
        chatTextBox.setAlignment(Pos.BOTTOM_CENTER);

        textFlowArea.setStyle("-fx-background-color: white; -fx-font-size: 14px;");
        textContainer.setStyle("-fx-background-radius: 5px;");
        textFlowArea.setPrefHeight(400);
        textFlowArea.setPrefWidth(560);

        //LEFT PANEL, MESSAGE AREA:
        HBox messageBox = new HBox(10);
        messageBox.setPadding(new Insets(10,0,15,0));
        messageBox.getChildren().addAll(messageTextArea,sendButton);
        messageTextArea.setPrefHeight(60);
        messageTextArea.setPrefWidth(506);
        messageTextArea.setWrapText(true);
        messageTextArea.setStyle("-fx-background-radius: 5px");
        sendButton.setPrefHeight(60);

        chatTextBox.getChildren().addAll(chatTextLabel, textContainer, messageBox);

        //INIT MAIN PANEL: ---------------------------------------------------------------------------------------------
        mainPane = new BorderPane();
        mainPane.setTop(connectPane);
        mainPane.setRight(userlistBox);
        mainPane.setLeft(chatTextBox);
    }

    /**
     * Different methods for appending text to the TextFlow depending on that color and style you want. ****************
     */
    public void appendOrangeBold(String msg){
        append(msg+"\n", "-fx-font-weight: 700; -fx-fill: #d58300;");
    }

    public void appendRed(String msg){
        append(msg+"\n", "-fx-fill: red");
    }

    public void appendGreen(String msg){
        append(msg+"\n", "-fx-fill: green");
    }

    public void appendBlue(String msg){
        append(msg+"\n", "-fx-fill: blue");
    }

    public void appendPurpleBold(String msg){
        append(msg+"\n", "-fx-fill: purple;");
    }

    public void appendRegular(String msg){
        append(msg+"\n", "");
    }

    /**
     * The append-methods then sends the message to this method for appending to the textflow. *************************
     * Creates new text-object with the chosen style from the methods above.
     *
     * @param msg = message to append.
     * @param style = css-style-code.
     */
    private synchronized void append(String msg, String style) {
        Platform.runLater(() -> {
            Text t = new Text(msg);
            if (!style.equals("")) {
                t.setStyle(style);
            }
            textFlowArea.getChildren().add(t);
        });
    }

    /**
     * Getters and setters. Gets information from the clients window. **************************************************
     */
    public String getIp(){
        return ipTextfield.getText();
    }

    public int getPort(){
        return Integer.parseInt(portTextfield.getText());
    }

    public String getUsername(){
        return usernameTextfield.getText();
    }

    public String getMessageTextArea(){
        return messageTextArea.getText();
    }

    public void setUserList(ObservableList currentUsers){
        userList.setItems(currentUsers);
    }

    public void clearMessageTextArea(){
        messageTextArea.clear();
    }

    /**
     *Initilize the client-window when connected. **********************************************************************
     */
    public void initConnected(){
        connectButton.setDisable(true);
        usernameTextfield.setDisable(true);
        ipTextfield.setDisable(true);
        portTextfield.setDisable(true);

    }

    /**
     * Method that detects if there are empty textfields when connecting.
     * Sets the empty textfield to a red background.
     * @return = True/False depending on if there are empty fields or not.
     */
    public boolean emptyFields(){
        boolean readyToConnect;

        String backgroundColor = "-fx-control-inner-background: rgba(255,0,0,0.4);";

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

    /**
     * Listeners, some are handled in the controller-class and some lighter "GUI-listeners" are handled here. **********
     */
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

    void guiListeners(){
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
    }
}
