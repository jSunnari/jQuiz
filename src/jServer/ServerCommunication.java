package jServer;

import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * Server-Communication-class - Scanner which listens for new messages.
 *
 * Command-flags:
 * /_DISCONNECT = A client has disconnected.
 * !startQuiz = A client has started the quiz.
 * RIGHTANSWER = A client has answered the question correctly.
 */

/**
 * Created by Jonas on 2016-02-17.
 */

public class ServerCommunication implements Runnable {

    //Holding the socket sent from the Server-class.
    private Socket socket;

    //Constructor with socket as imparameter:
    public ServerCommunication(Socket socket) {
        this.socket = socket;
    }

    /**
     * Run method for Server-Communication: ****************************************************************************
     */
    @Override
    public void run() {
        try{
            /***********************************************************************************************************
             *The Communication-class will now do different things depending on what command comes through the stream
             **********************************************************************************************************/
            //Input-scanner from socket-stream:
            Scanner input = new Scanner(socket.getInputStream());

            //while the stream is alive:
            while (input.hasNext())
            {
                //If there is a message in the inputstream, save it into a variable:
                String message = input.nextLine();

                //If a client disconnects, remove from arraylist and update the onlineusers-list:
                if(message.startsWith("/_DISCONNECT")){
                    //Remove "Command-flag" and timestamp:
                    message = message.substring(12);
                    //Methods in Server-class:
                    Server.removeUser(message);
                    Server.sendUserList();
                }

                //If the client starts a quiz:
                else if(message.startsWith("!startQuiz")){
                    Server.sendBotMessage(message.substring(10) + " has started the quiz.");
                    Server.startQuiz();
                }

                else if (message.startsWith("!getScores")){
                    Server.sendBotMessage("!getScores");
                }

                //Else, it is a usual message, send it with a timestamp:
                else {
                    DateFormat df = new SimpleDateFormat("HH:mm");
                    Date dateObj = new Date();
                    message = "[" + df.format(dateObj) + "] " + message;
                }

                //Except if its a startQuiz-command, do this:
                //Send the message to the server(console):
                //Echo out the message to every client in the connectionarray:
                if (!message.startsWith("!startQuiz") && !message.startsWith("!getScores")) {
                    System.out.println(message + "\n");
                    for (int i = 0; i < Server.connectionArray.size(); i++) {
                        Socket tempSock = Server.connectionArray.get(i);
                        PrintWriter tempOut = new PrintWriter(tempSock.getOutputStream());
                        tempOut.println(message);
                        tempOut.flush();
                    }
                }

                //If the quiz is ON and if the right answer for the current question it written:
                if (Server.getQuizIsOn() && message.substring(message.indexOf(" ",8)+1).equalsIgnoreCase(Server.getAnswer())){
                    //Command-flag and send which user guessed right:
                    message = "RIGHTANSWER" + "Winner: " + message.substring(8,message.indexOf(":",8));
                    Server.sendBotMessage(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
