package jClient;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

/**
 * Created by Jonas on 2016-02-17.
 */

public class ClientModel implements Runnable{

    private Socket socket;
    private Scanner input;
    private PrintWriter output;
    private String userName;
    private boolean connected = false;
    private ObservableList<String> userList = FXCollections.observableArrayList();
    private ListView<String> userListView = new ListView<>(userList);
    private String message;
    private boolean sendMessage = false;



    //run-method for client(setting up input and output, start receiving messages:--------------------------------------
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

                //ClientView.chatTextArea.appendText("You connected to host: " + host + "\n\n");


                input = new Scanner(socket.getInputStream());
                output = new PrintWriter(socket.getOutputStream());
                output.flush();
                CheckStream();
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

    //Connect-method (port and host as imparameter, connection timeout on 3s)-------------------------------------------
    public void connect(int port, String host)
    {
        try
        {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 3000);

            //Sends the username to the server, sets connected-boolean to true:
            output = new PrintWriter(socket.getOutputStream());
            output.println(userName);
            output.flush();
            connected = true;
        }
        catch(Exception e)
        {
            System.out.println("Server is not responding.");
            e.printStackTrace();
        }
    }


    //Disconnect-method-------------------------------------------------------------------------------------------------
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

                    // /_DISCONNECT so the server knows its a disconnection:
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


    //Disconnect-method for shutdownhook, called from main-method:------------------------------------------------------
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


    //Method for while-looping the receive-method-----------------------------------------------------------------------
    public void CheckStream()
    {
        while(true)
        {
            receive();
        }
    }

    //Receive message-method--------------------------------------------------------------------------------------------
    public void receive (){
        try {
            if(input.hasNext())
            {

                /***********************************************************************************************************
                 *The ClientModel will now do different things depending on what command comes through the stream
                 **********************************************************************************************************/

                message = input.nextLine();
                int messageLength = message.length();

                //This is a command that comes with a username:
                if(message.startsWith("/_USERNAME")) {
                    System.out.println("allt: " +  message);

                    //Remove /_USERNAME and [], split with commas, fill array:
                    String user = message.substring(11);
                    user = user.replace("[", "");
                    user = user.replace("]", "");
                    System.out.println("efter " + user);

                    userList = FXCollections.observableArrayList(user.split(", "));

                    System.out.println("listan: " + userList);

                    //Send list to Listview (Online users): - Platform.runlater prevents thread-collision:
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            ClientView.userList.setItems(userList);
                        }
                    });
                }

                //This is a command that tells if the username already exists:
                else if (message.startsWith("/_EXISTS")){

                    //Send a message and change the username:
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("jQuiz");
                    alert.setHeaderText(null);
                    alert.setContentText("Username already exists, changing your username..");
                    alert.showAndWait();

                    String newUser = message.substring(10);
                    userName = newUser;
                }

                //This is a command that tells if the server disconnects:
                else if(message.startsWith("/_SERVERDISCONNECT")){
                    disconnectedByServer();
                }
                else if(message.startsWith("/_CONNECT")){
                    message = message.substring(9);
                    ClientView.appendGreen(message + "\n");
                }

                else if(message.startsWith("/_BOT")){

                    if(message.startsWith("/_BOTQUESTION")){
                        message = message.substring(13);
                        ClientView.appendOrangeBold("[jQuiz] " + message + "\n");
                    }
                    else {
                        message = message.substring(5);
                        ClientView.appendPurpleBold("[jQuiz] " + message + "\n");
                    }
                }

                else if(message.startsWith("Welcome")){
                    ClientView.appendBlue(message + "\n");
                }



                //It neither above, this is a usual message, send as it is:
                else {
    //                String user = message.substring(8, message.indexOf(" ", 8));
    //                int userLength = user.length();

                    //If it's a chatmessage:
                    if (message.startsWith("[")) {

                        ClientView.appendRegular(message + "\n");


                    }
                    //If message is a "Client has disconnected"-message:
                    else {
                        System.out.println("else " + message);

                        ClientView.appendRed(message + "\n");



                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //Send message-method (Streams out message)-------------------------------------------------------------------------
    public void send (String message) {
        if (message.equals("!startQuiz")){
            output.println(message + userName);
        }
        else {
            output.println(userName + ": " + message);
        }
        output.flush();
    }

    //What to do when client gets disconnected by server:---------------------------------------------------------------
    private void disconnectedByServer(){

        try {
            socket.close();
            connected = false;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Set username:
    public void setUserName(String userName){
        this.userName = userName;
    }

    //Get username:
    public String getUserName(){
        return userName;
    }

    //Get connected-boolean:
    public boolean getConnected(){
        return connected;
    }

    public String getMessage(){
        return message;
    }

    public ObservableList<String> getUserList(){
        return userList;
    }

    //test:

    void onChangedListener (ListChangeListener<String> changeListener){
        System.out.println("hello?");
        userList.addListener(changeListener);
    }

    public ListView<String> getUserListView(){
        return userListView;
    }

    public Scanner getInput(){
        return input;
    }




}
