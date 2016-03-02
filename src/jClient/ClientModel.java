package jClient;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

/**
 * The model for the client with all the logic for the client-application.
 * Connects and disconnects to server, receives and sends messages.
 *
 * Command-flags:
 * /_DISCONNECT = A client has disconnected.
 * /_BOT = Regular bot-message.
 *  /_BOTQUESTION = question from bot.
 *  /_BOTSTRGAME = bot tells client that game has started.
 *  /_BOTENDGAME = bot tells clients that game has ended.
 * !startQuiz = A client has started the quiz.
 * RIGHTANSWER = A client has answered the question correctly.
 */

/**
 * Created by Jonas on 2016-02-17.
 */

public class ClientModel implements Runnable{

    //Variables for messaging (Socket, Scanner and PrintWriter):
    private Socket socket;
    private Scanner input;
    private PrintWriter output;
    //String holding the username:
    private String userName;
    //Boolean showing if client is connected or not:
    private boolean connected = false;
    //ObservableList holding all online users:
    private ObservableList<String> userList = FXCollections.observableArrayList();
    //String holding the current message:
    private String message;
    //Reference to the interface messageInterface:
    private MessageInterface messageInterface;

    /**
     * Constructor for the ClientModel, gets a reference to the messageInterface via the constructor.
     * @param messageInterface = reference to the interface "MessageInterface".
     */
    public ClientModel(MessageInterface messageInterface) {
        this.messageInterface = messageInterface;
    }

    /**
     * run-method for client(setting up the input, start receiving messages. *******************************************
     */
    @Override
    public void run() {
        try
        {
            try
            {
                String host = socket.getInetAddress().toString();
                if (host.startsWith("/")){
                    host = host.substring(1);
                }

                //Shows what host you have connected to:
                message = "You connected to host: " + host + "\n";
                messageInterface.appendRegular();

                //Set up input and start receiving messages:
                input = new Scanner(socket.getInputStream());
                checkStream();
            }
            finally
            {
                socket.close();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Method for connecting to the server, connection timeout 3sec. Called from the ClientController-class.
     * If the server doesn't respond, send a message to the client.
     * @param port = port from the GUI-textfield.
     * @param host = host from the GUI-textfield
     */
    public void connect(int port, String host)
    {
        try
        {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 3000);

            //Set up the output, send the username to the server, set connected-boolean to true:
            output = new PrintWriter(socket.getOutputStream());
            output.println(userName);
            output.flush();
            connected = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("jQuiz");
            alert.setHeaderText(null);
            alert.setContentText("Server is not responding.");
            alert.showAndWait();
        }
    }

    /**
     * Method for disconnecting. Called from the ClientController-class.
     */
    public void disconnect() {
        try {
            //If connected - Check if client really wants to disconnect:
            if (connected) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("jQuiz");
                alert.setHeaderText("Disconnect - application will now close");
                alert.setContentText("Are you sure?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    // /_DISCONNECT-flag so the server knows its a disconnection:
                    output.println("/_DISCONNECT" + userName + " has disconnected.");
                    output.flush();
                    socket.close();
                    connected = false;
                    System.exit(0);
                }
            }
            //else if client is not connected, send a message:
            else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("jQuiz");
                alert.setHeaderText(null);
                alert.setContentText("You are not connected.");
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shutdown-hook. This method is called from the ClientController-class just before the application closes.
     */
    public void shutDownDisconnect(){
        try {
            output.println("/_DISCONNECT" + userName + " has disconnected.");
            output.flush();
            socket.close();
            Platform.exit();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for while-looping the receive-method. ********************************************************************
     */
    public void checkStream()
    {
        while(true)
        {
            receive();
        }
    }

    /**
     * Method for receiving messages.
     * Uses different flags to control what is going to happend with the message that comes through. *******************
     */
    public void receive (){
        try {
            if(input.hasNext())
            {
                /*******************************************************************************************************
                 *The receive-method will now do different things depending on what command comes through the stream
                 ******************************************************************************************************/

                //save the message to a variable:
                message = input.nextLine();

                //If a new client has connected, add the username to the listview:
                if(message.startsWith("/_USERNAME")) {

                    //Remove /_USERNAME-flag and [], split with commas, fill the userlist:
                    String user = message.substring(11);
                    user = user.replace("[", "");
                    user = user.replace("]", "");
                    userList = FXCollections.observableArrayList(user.split(", "));

                    //Send list to Listview by calling method in the messageInterface (Online users).
                    //Platform.runlater prevents thread-collision:
                    Platform.runLater(() -> messageInterface.sendUserList());
                }

                //This is a command that tells if the chosen username already exists:
                else if (message.startsWith("/_EXISTS")){

                    Platform.runLater(() -> {
                        //Send a message and change the username:
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("jQuiz");
                        alert.setHeaderText(null);
                        alert.setContentText("Username already exists, changing your username..");
                        alert.showAndWait();
                    });
                    //Sets the username to a new that the server has generated:
                    userName = message.substring(8);
                    //Call the interface to change the username-textfield in the GUI-window.
                    messageInterface.changeUserName();
                }

                //If a client has connected, append in green color:
                else if(message.startsWith("/_CONNECT")){
                    message = message.substring(9);
                    messageInterface.appendGreen();
                }
                //If its a welcome-message from the server, append in blue:
                else if(message.startsWith("Welcome")){
                    messageInterface.appendBlue();
                }

                //These are flags for bot-messages:
                else if(message.startsWith("/_BOT")){

                    //If it is a question, append in bold organe:
                    if(message.startsWith("/_BOTQUESTION")){
                        message =  "[jQuiz] " + message.substring(13);
                        messageInterface.appendOrangeBold();
                    }
                    //If the game ends or starts, append in blue:
                    else if(message.startsWith("/_BOTENDGAME") || message.startsWith("/_BOTSTRGAME")){
                        message = "[jQuiz] " + message.substring(12);
                        messageInterface.appendBlue();
                    }
                    //If neither above, append in bold purple:
                    else {
                        message = "[jQuiz] " + message.substring(5);
                        messageInterface.appendPurpleBold();
                    }
                }

                //It neither above, this is a usual message, send as it is:
                else {
                    //If it's a chatmessage, append in regular text:
                    if (message.startsWith("[")) {
                        messageInterface.appendRegular();
                    }

                    //If message is a "Client has disconnected"-message, append in red:
                    else {
                        messageInterface.appendRed();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for sending a message to other clients. Called from ClientController.
     * @param message = the message from the GUI-window.
     */
    public void send (String message) {
        if (message.equals("!startQuiz") || message.equals("!getScores")){
            output.println(message + userName);
        }
        else {
            output.println(userName + ": " + message);
        }
        output.flush();
    }

    //Set username:
    public void setUserName(String userName){
        this.userName = userName;
    }

    //Get username, used with the MessageInterface in ClientController.
    public String getUserName(){
        return userName;
    }

    //Get "connected or not"-boolean:
    public boolean getConnected(){
        return connected;
    }

    //Get the current message, used with the MessageInterface in ClientController.
    public String getMessage(){
        return message;
    }

    //Get the "online users-list", used with the MessageInterface in ClientController.
    public ObservableList<String> getCurrentUsers(){
        return userList;
    }
}
