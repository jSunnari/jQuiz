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
import java.util.*;

/**
 * Server-class, sets up a connection for the server - Listens to new clients.
 * Holds all user-sockets and usernames and Runs the quiz-logic.
 *
 * Command-flags:
 * /_EXISTS = The username a client has chosen already exists, renames the user to a unique name.
 * /_CONNECT = A client has connected.
 * /_USERNAME = Usernames for online clients.
 * /_BOT = A bot message to the clients.
 * RIGHTANSWER = A client has answered a question correctly.
 * QUESTION = A question from the bot.
 */

/**
 * Created by Jonas on 2016-02-17.
 */

public class Server implements Runnable {

    //Arraylists holding connections and users:
    public static ArrayList<Socket> connectionArray = new ArrayList<>();
    private static ObservableList<String> currentUsers = FXCollections.observableArrayList();
    private static ArrayList<Integer> scoreList = new ArrayList<>();

    //Counter for usernames that has the same name (making them unique).
    private int counter = 1;

    //For timestamp.
    private DateFormat df = new SimpleDateFormat("HH:mm");
    private Date dateObj = new Date();


    //static Quiz-variables (communicates with the "ServerCommunication"-class):
    private static String answer; // string that is holding the current answer.
    private static int questionNumber = 0; //int for holding which the current question is.
    private static ArrayList<String> questionList; //arraylist for all questions.
    private static ArrayList<Integer> randomNumbers = new ArrayList<>(); //arraylist holding randomnumbers.
    private static boolean questionAnswered; //boolean which knows if a questions is answered or not.
    private static Timeline timeline;
    private static boolean quizIsOn;

    /**
     * Run method for server: ******************************************************************************************
     */
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

            //While listening for new clients to join:
            while (true) {
                //Saves the connection into a socket, also save the socket in the arraylist:
                Socket socket = serverSocket.accept();
                connectionArray.add(socket);
                //Add username to socket with the method addUserName:
                addUserName(socket);
                //Start a thread for the "ServerCommunication"-class (start listening for messages):
                ServerCommunication chat = new ServerCommunication(socket);
                Thread communication = new Thread(chat);
                communication.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will get the username from the connecting client. ***************************************************
     * @param x = Socket received from the run()-method.
     */
    public void addUserName(Socket x){
        try {
            //Create a scanner and save the incomming message from client as a username:
            Scanner input = new Scanner(x.getInputStream());
            String userName = input.nextLine();

            //If username doesn't exists, add as it is:
            if (!currentUsers.contains(userName)) {
                currentUsers.add(userName);
                scoreList.add(currentUsers.indexOf(userName),0);
                //TEST

                for (int i = 0; i < scoreList.size(); i++) {
                    System.out.println(scoreList.get(i));
                }


            }

            //If username already exists, change to alternative unique name.
            //Send the new username to the client(with a flag):
            else{
                String newUserName = userName + "(" + counter + ")";
                currentUsers.add(newUserName);
                scoreList.add(currentUsers.indexOf(userName),0);
                userName = newUserName;
                PrintWriter out = new PrintWriter(x.getOutputStream());
                out.println("/_EXISTS" + newUserName);
                out.flush();
                counter++;
            }

            //Method that echos out to all connected clients, sends entire arraylist with users:
            sendUserList();

            //Show the server who has connected:
            String host = x.getInetAddress().toString();
            System.out.println(currentUsers.get(currentUsers.size() - 1)
                    + " has connected from " + host.substring(1) + "\n");

            //Sends out a message to every user except the client who just has connected (with a flag):
            for (int i = 0; i < connectionArray.size()-1; i++) {
                Socket tempSock = connectionArray.get(i);
                PrintWriter out = new PrintWriter(tempSock.getOutputStream());
                out.println("/_CONNECT" + userName + " has connected.");
                out.flush();
            }

            //send welcome-message to the client who just has connected:
            PrintWriter out = new PrintWriter(connectionArray.get(connectionArray.size()-1).getOutputStream());
            out.println("Welcome to jQuiz " + userName + "!\nType !startQuiz to start the quiz - !getScores to get scores.\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for removing a user from the arraylist. ******************************************************************
     * Substring removes the disconnect-flag.
     * @param userName = gets username from the "ServerCommunication"-class.
     */
    public static void removeUser(String userName) {
        userName = userName.substring(0, userName.length()-18);
        scoreList.remove(currentUsers.indexOf(userName));
        currentUsers.remove(userName);
    }

    /**
     * Sends current online users to all clients (with a flag): ********************************************************
     */
    public static void sendUserList(){
        try {
            for (Socket tempSock : connectionArray) {
                PrintWriter out = new PrintWriter(tempSock.getOutputStream());
                // /_USERNAME to separate the username from usual message.
                out.println("/_USERNAME" + currentUsers);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for sending jQuiz-bot-messages. **************************************************************************
     * @param message = message incomming from the "ServerCommunication"-class.
     */
    public static void sendBotMessage(String message){

        //Flag which says that the question has been answered. Changes the boolean.
        if (message.startsWith("RIGHTANSWER")){
            questionAnswered = true;

            String winningUser = message.substring(19);

            System.out.println("namn: " + winningUser);

            int indexOfWinningUser = currentUsers.indexOf(winningUser);

            System.out.println("index: " + indexOfWinningUser);

            scoreList.set(indexOfWinningUser,scoreList.get(indexOfWinningUser)+1);

            for (int i = 0; i < scoreList.size() ; i++) {
                System.out.println(scoreList.get(i));

            }

            double answeredTime = timeline.getCurrentTime().toSeconds();
            message = message.substring(11) + " - Time: " + (double)Math.round(answeredTime * 100d) / 100d + "sec - " +
                    "Points: " + scoreList.get(indexOfWinningUser);




            System.out.println(message);
        }

        else if (message.startsWith("!getScores")){
            for (int i = 0; i < currentUsers.size(); i++) {
                sendBotMessage(currentUsers.get(i) + " - " + scoreList.get(i) + "Points");
            }
        }

        //Sends out a regular bot-message to all clients (except if its a call for getting scores):
        if (!message.startsWith("!getScores")) {
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
    }

    /**
     * Method for starting a quiz. *************************************************************************************
     * Reads the file questions.txt and adds all the questions and answers into a arraylist.
     * Question will get even indexnumbers in the arraylist and answers will get odd indexnumbers.
     * Fills the randomNumbers-arraylist.
     * Starts asking questions.
     */
    public static void startQuiz(){
        questionList = new ArrayList<>();
        quizIsOn = true;

        //Read file and fill arraylist:
        try {
            for (String line : Files.readAllLines(Paths.get("src/questions.txt"))) {
                Collections.addAll(questionList, line.split(","));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Fill the randomNumbers-arraylist with as many random numbers that there is questions. All unique numbers.
        while (randomNumbers.size() < questionList.size()/2){
            int rndNmr = rndNumber();
            if (!randomNumbers.contains(rndNmr)) {
                randomNumbers.add(rndNmr);
            }
        }

        //Start asking questions:
        askQuestion();
    }

    /**
     * Method that asks questions. *************************************************************************************
     * Asks a random question, saves the answer in the string-variable "answer".
     * Starts timer.
     */
    public static void askQuestion(){
        System.out.println("Question number: " + questionNumber+1);
        questionAnswered = false;
        sendBotMessage("QUESTION" + questionList.get(randomNumbers.get(questionNumber)));
        System.out.println(questionList.get(randomNumbers.get(questionNumber)));
        answer = questionList.get(randomNumbers.get(questionNumber)+1);

        //Starts time-ticking:
        timeTicker();
    }

    /**
     * Method that works as a timer for the questions. *****************************************************************
     * If the boolean "questionsAnswered" is false, the bot will give the correct answer.
     * If the current question-number is lower than the amount of questions there is,
     * ask a new question and increase the value the current question-number.
     * Else if the questionnumber is going to set the arraylist of index out of bounce,
     * clear the questionnumber and the randomNumbers-arraylist and send a message that the game is now over.
     */
    public static void timeTicker(){
        timeline = new Timeline(new KeyFrame(
                Duration.millis(4000),
                ae -> {
                    if (!questionAnswered) {
                        sendBotMessage("Times up, the right answer was " + answer);
                    }

                    if (questionNumber < randomNumbers.size()-1){
                        questionNumber++;
                        askQuestion();
                    }
                    else{
                        int winnerIndex = scoreList.indexOf(Collections.max(scoreList));
                        System.out.println(winnerIndex);

                        sendBotMessage("End of game. Winner with " + Collections.max(scoreList) + " points: " + currentUsers.get(winnerIndex));
                        quizIsOn = false;
                        questionNumber = 0;
                        randomNumbers.clear();
                    }
                }));
        timeline.play();

    }

    /**
     * Method for generating only even randomNumbers. ******************************************************************
     * @return = even random numbers for the randomNumber-arraylist.
     */
    public static int rndNumber(){
        Random rand = new Random();
        int randomNum = rand.nextInt(questionList.size()/2) *2;

        return randomNum;
    }

    /**
     * Method that finds the external IP of the server. ****************************************************************
     * @return = ip-number as a string.
     */
    private String externalIp (){
        String ip = "";
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

            ip = in.readLine(); //you get the IP as a String
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ip;
    }

    /**
     * Method for catching the current answer of a question (used in ServerCommunication-class): ***********************
     * @return = answer for the current question, as a string.
     */
    public static String getAnswer(){
        return answer;
    }

    /**
     * Method for knowing if the quiz is on: ***************************************************************************
     * @return = true/false if the quiz is going on right now.
     */
    public static boolean getQuizIsOn(){
        return quizIsOn;
    }

}