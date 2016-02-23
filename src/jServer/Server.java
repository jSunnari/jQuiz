package jServer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Jonas on 2016-02-17.
 */

public class Server implements Runnable {

    //Arraylists holding connections and users:
    public static ArrayList<Socket> connectionArray = new ArrayList<>();
    private static ObservableList<String> currentUsers = FXCollections.observableArrayList();
    //Counter for usernames that has the same name (making them unique).
    private int counter = 1;
    //For timestamp.
    private DateFormat df = new SimpleDateFormat("HH:mm");
    private Date dateObj = new Date();
    private static String answer;
    private static int questionNumber = 0;
    private static ArrayList<String> questionList;
    private static boolean questionAnswered;
    private static ArrayList<Integer> randomNumbers = new ArrayList<>();



    public Server() {



    }

    //run-method for server:--------------------------------------------------------------------------------------------
    public void run () {
        try {
            //Setting up a serversocket that listens to port, waiting for clients:
            int port = 50123;
            ServerSocket serverSocket = new ServerSocket(port);

            //Information will be shown in serverwindow:
            System.out.println("Loopback address: " + InetAddress.getLoopbackAddress());
            System.out.println("Local address: " + InetAddress.getLocalHost());
            System.out.println("External address: " + externalIp() + "\n");
            System.out.println("\n[" + df.format(dateObj) + "] Server started on port: " + port + ", waiting for clients... \n");

            //While listening:
            while (true) {
                //Saves the connection into a socket, also save it in the arraylist:
                Socket socket = serverSocket.accept();
                connectionArray.add(socket);
                //Add username to socket with method:
                addUserName(socket);
                //Make new object for socketconnection and start a thread for the communication-class (start listening):
                ServerCommunication chat = new ServerCommunication(socket);
                Thread communication = new Thread(chat);
                communication.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Take the username streamed out from ClientModel, turn into string and add to ArrayList:---------------------------
    public void addUserName(Socket x) throws IOException {

        Scanner input = new Scanner(x.getInputStream());
        String userName = input.nextLine();

        //If username doesn't exists, add as it is:
        if (!currentUsers.contains(userName)) {
            currentUsers.add(userName);
        }

        //If username already exists, change to alternative name. Send new username to client(with command):
        else{
            String newUserName = userName + "(" + counter + ")";
            currentUsers.add(newUserName);
            userName = newUserName;
            PrintWriter out = new PrintWriter(x.getOutputStream());
            out.println("/_EXISTS" + newUserName);
            out.flush();
            counter++;
        }

        //Echos out to all users, send entire arraylist with users:
        sendUserList();

        //Show the server who has connected:
        String host = x.getInetAddress().toString();
        System.out.println(currentUsers.get(currentUsers.size() - 1)
                + " has connected from " + host.substring(1) + "\n");

        //Sends out to every user except the client who has connected:
        for (int i = 0; i < connectionArray.size()-1; i++) {
            Socket tempSock = connectionArray.get(i);
            PrintWriter out = new PrintWriter(tempSock.getOutputStream());
            out.println("/_CONNECT" + userName + " has connected.");
            out.flush();
        }

        //send welcome-message to the client who just has connected:
        PrintWriter out = new PrintWriter(connectionArray.get(connectionArray.size()-1).getOutputStream());
        out.println("Welcome to jQuiz " + userName + ", type !startQuiz to start the quiz.\n");
        out.flush();
    }


    //Method for removing user from ArrayList (removes "has disconnected."-flag from string)---------------------------------
    public static void removeUser(String userName) {
        userName = userName.substring(0, userName.length()-18);
        currentUsers.remove(userName);
    }


    //Sends current online users to all clients(with command):----------------------------------------------------------
    public void sendUserList(){
        try {
            for (Socket tempSock : connectionArray) {
                PrintWriter out = new PrintWriter(tempSock.getOutputStream());
                // /_USERNAME to separate the username from usual message, the Client will now know:

                out.println("/_USERNAME" + currentUsers);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendBotMessage(String message){
        if (message.startsWith("RIGHTANSWER")){
            questionAnswered = true;
            message = message.substring(11);
        }


        System.out.println(message);
        try {
            for (Socket tempSock : connectionArray) {
                PrintWriter tempOut = new PrintWriter(tempSock.getOutputStream());
                tempOut.println("/_BOT" + message);
                tempOut.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void startQuiz(){
        questionList = new ArrayList<>();
        //questionNumber = rndNumber();



        try {
            for (String line : Files.readAllLines(Paths.get("/Users/Jonas/Documents/IdeaProjects/JavaFX/jQuiz/src/questions.txt"))) {

                for (String part : line.split(",")) {
                    questionList.add(part);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        while (randomNumbers.size() < questionList.size()/2){
            int rndNmr = rndNumber();
            if (!randomNumbers.contains(rndNmr)) {
                randomNumbers.add(rndNmr);
                System.out.println("loopen: " + rndNmr);
            }
        }




        askQuestion();

    }

    public static void askQuestion(){
        System.out.println("fråga nummer: " + questionNumber);

        questionAnswered = false;
        sendBotMessage("QUESTION" + questionList.get(randomNumbers.get(questionNumber)));
        System.out.println(questionList.get(randomNumbers.get(questionNumber)));
        answer = questionList.get(randomNumbers.get(questionNumber)+1);
        timeTicker();
    }

    public static int rndNumber(){
        Random rand = new Random();
        int randomNum = rand.nextInt(questionList.size()/2) *2;

        return randomNum;
    }

    public static void timeTicker(){


        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(5000),
                ae -> {
                    if (!questionAnswered) {
                        sendBotMessage("Times up, the right answer was " + answer);
                    }

                    if (questionNumber < randomNumbers.size()-1){
                        questionNumber++;
                        askQuestion();
                    }
                    else{
                        sendBotMessage("End of game.");
                        questionNumber = 0;
                        randomNumbers.clear();
                    }

                    }));
            timeline.play();


    }

    //Find the external IP of the server:-------------------------------------------------------------------------------
    private String externalIp () throws IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

        String ip = in.readLine(); //you get the IP as a String
        return ip;
    }

    public static String getAnswer(){
        return answer;
    }

    public static void setQuestionAnswered(boolean answered){
        questionAnswered = answered;
    }


}
