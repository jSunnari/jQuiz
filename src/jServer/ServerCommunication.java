package jServer;

import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by Jonas on 2016-02-17.
 */

public class ServerCommunication implements Runnable {
    //holding the socket.
    private Socket socket;

    //constructor with socket as imparameter:
    public ServerCommunication(Socket socket) {
        this.socket = socket;
    }


    //run-method for ServerCommunication:-------------------------------------------------------------------------------------
    @Override
    public void run() {
        try{
            /***********************************************************************************************************
             *The Communication-class will now do different things depending on what command comes through the stream
             **********************************************************************************************************/
            //Input-scanner from socket-stream:
            Scanner input = new Scanner(socket.getInputStream());

            //while there stream is alive:
            while (input.hasNext())
            {
                //If there is a message in the inputstream, save it into a variable:
                String message = input.nextLine();

                //If a client disconnects, remove from arraylist and update the clientlist:
                if(message.startsWith("/_DISCONNECT")){
                    //Remove "Command-flag and timestamp":
                    message = message.substring(12);
                    Server.removeUser(message);
                }

                else if(message.startsWith("!startQuiz")){
                    Server.sendBotMessage(message.substring(10) + " has started the quiz.");
                    Server.startQuiz();


                    //starta quiz

                }
                //Else, it is a usual message, send it with a timestamp:
                else {
                    DateFormat df = new SimpleDateFormat("HH:mm");
                    Date dateObj = new Date();
                    message = "[" + df.format(dateObj) + "] " + message;
                }

                //Send the message to the server(console):
                //Echo out the message to every client in the connectionarray:
                if (!message.startsWith("!startQuiz")) {
                    System.out.println(message + "\n");
                    for (int i = 0; i < Server.connectionArray.size(); i++) {
                        Socket tempSock = Server.connectionArray.get(i);
                        PrintWriter tempOut = new PrintWriter(tempSock.getOutputStream());
                        tempOut.println(message);
                        tempOut.flush();
                    }
                }

                //TEST
                // if (message.substring(message.indexOf(" ",8)+1).equals("7428954")){
                if (message.substring(message.indexOf(" ",8)+1).equalsIgnoreCase(Server.getAnswer())){

                    message = "RIGHTANSWER" + message.substring(8,message.indexOf(":",8)) + " svarade rätt!";

                    Server.sendBotMessage(message);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
