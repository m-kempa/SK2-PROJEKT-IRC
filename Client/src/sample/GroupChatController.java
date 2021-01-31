package sample;


import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import java.util.*;

import java.text.Normalizer;
import javafx.scene.text.Text;
import sample.ConnectionToServer;
import sample.ApiWidnowController;

import java.io.IOException;

import static sample.Main.END;

//Klasa obsługująca główne okno aplikacji IRC
public class GroupChatController {

    @FXML
    private TextField roomName;
    @FXML
    private Button newRoomButton;
    @FXML
    private Button deleteRoomButton;
    @FXML
    private Button waitRoom;
    @FXML
    private Button firstSport;
    @FXML
    private Button secondStudy;
    @FXML
    private Button thirdWork;
    @FXML
    private Button fourth;
    @FXML
    private Button fifth;
    @FXML
    private TextField messageWriting;
    @FXML
    private TextArea messageDisplay;

    private boolean isFourthRoomAdded = false;
    private boolean isFifthRoomAdded = false;
    private ConnectionToServer serwer = new ConnectionToServer();

    private int chatRoomNumber = 0;

    List<String> historyWaitingRoom = new ArrayList<String>();
    List<String> historyFirstRoom = new ArrayList<String>();
    List<String> historySecondRoom = new ArrayList<String>();
    List<String> historyThirdRoom = new ArrayList<String>();
    List<String> historyFourthRoom = new ArrayList<String>();
    List<String> historyFifthRoom = new ArrayList<String>();



    //Metoda wysyłająca do serwera wiadomość o stworzeniu nowego pokoju
    private void sendNewRoomToServer(String chat_number, String room_name) {
        messageDisplay.clear();
        messageWriting.clear();
        roomName.clear();
        chatRoomNumber = Integer.parseInt(chat_number);
        String length = RoomZeroAdd(room_name);
        if(chatRoomNumber == 4 || chatRoomNumber == 5){
            //tworzenie nowego wątku w celu wpółbierznośći w słaniu zapytań o nadanie nazwy wolnemu pokojowi
            Thread NewRoom = new Thread(() -> {
                try {
                    serwer.roomAdd("aRoom" + length + room_name );
                } catch (IOException e) {
                    ;
                }
            });
            NewRoom.start();
        }
    }

    //Metoda wysyłająca do serwera wiadomość o usunięciu pokoju
    private void sendDeleteRoomToServer(String chat_number, String room_name) {
        messageDisplay.clear();
        messageWriting.clear();
        roomName.clear();
        chatRoomNumber = Integer.parseInt(chat_number);
        String length = RoomZeroAdd(room_name);
        if(chatRoomNumber == 4 || chatRoomNumber == 5){
            //tworzenie nowego wątku w celu wpółbierznośći w słaniu zapytań usunięcie pokoju
            Thread DeleteRoom = new Thread(() -> {
                try {
                    serwer.roomDel("dRoom" + length + room_name );
                } catch (IOException e) {
                    ;
                }
            });
            DeleteRoom.start();
        }
    }

    // metoda służąca uzupełnieniu długości nazwy pokoju zerem jeżeli jego długość jest mnijsza do 10
    private String RoomZeroAdd(String login) {
        String length;
        if (login.length() < 10)
            length = "0" + Integer.toString(login.length());
        else
            length = Integer.toString(login.length());
        return length;
    }

    //Metoda powoduje wysłanie wiadomości przez użytkonika po wciśnięciu przycisku ENTER
    public void sendMessageWhenEnter(ActionEvent actionEvent) throws IOException{

        //Można wysłać tylko niepustą wiadomość
        if(!messageWriting.getText().isEmpty()) {

            //tworzenie nowego wątku, aby współbierznie wysyłać wiadomości
            Thread Message = new Thread(() -> {
                try {
                    serwer.sendMessage(messageWriting.getText());
                    //System.out.println(messageWriting.getText());
                    //messageWriting.clear();
                } catch (IOException e) {
                    ;
                }
            });
            Message.start();
        }
        roomName.clear();
    }

    //Metoda wysyłająca do serwera wiadomość o zmiane pokoju
    private void sendRoomToServer(String chat_number) {
        messageDisplay.clear();
        messageWriting.clear();
        chatRoomNumber = Integer.parseInt(chat_number);

        //tworzenie nowego wątku w celu wpółbierznośći w słaniu zapytań o zmianę pokoju
        Thread RoomChange = new Thread(() -> {
            try {
                serwer.roomChange("cRoom" + chat_number);
            } catch (IOException e) {
                ;
            }
        });
        RoomChange.start();
    }

    //Metoda inicializująca dla javaFX
    public void initialize(){
        //Nowy wątek w celu współbierzności, który działa aż do zakończenia programu
        Thread initialize = new Thread(() -> {
            String nickLen;
            String messLen;
            String message;
            String roomNumber;
            String NewRoomLen;

            //Opcje Komunikatów od serwera
            String messageFromOtherUserInRoom = "messU";
            String userJoinToRoom  = "roomJ";
            String userLeaveRoom = "roomL";
            String userAddRoom = "roomA";
            String userDeleteRoom = "roomD";

            //messageWriting.setDisable(true);
            //messageDisplay.setWrapText(true);
            messageDisplay.clear();
            messageWriting.clear();

            messageDisplay.setText("\t\t\t\t\t\t\tWelcome to IRC Communicator\n" +
            "\t\t\t\t\t\t\t\t\tRules:\n" +
            "\t\t\t\t\t\t\tPlease do not use Polish words!\n\n\n" );
            historyWaitingRoom.add("\t\t\t\t\t\t\tWelcome to IRC Communicator\n" +
                    "\t\t\t\t\t\t\t\t\tRules:\n" +
                    "\t\t\t\t\t\t\tPlease do not use Polish words!\n\n\n" );

            while (!END) {
                try {

                    message = serwer.messageGetFromServer();

                    //odzielenie wiadomości od serwera na nick wysyłającego i treść wiadomości do chatu
                    if (messageFromOtherUserInRoom.equals(message.substring(0,5))) {

                        message = message.substring(5, message.length());
                        nickLen = message.substring(0, 2);
                        nickLen = nickLen.replaceAll("^0*", "");
                        messLen = message.substring(Integer.parseInt(nickLen) + 2, Integer.parseInt(nickLen) + 5);
                        messLen = messLen.replaceAll("^0*", "");

                        //wyświetlenie nicku i wiadomości na ekranie
                        String finalMessage2 = message;
                        String finalNickLen = nickLen;
                        String finalMessLen = messLen;
                        Platform.runLater(() -> {
                            messageDisplay.appendText(finalMessage2.substring(2, 2 + Integer.parseInt(finalNickLen)) + " : \n");
                            messageDisplay.appendText("\t\t" + finalMessage2.substring(Integer.parseInt(finalNickLen) + 5, Integer.parseInt(finalNickLen) + 5 + Integer.parseInt(finalMessLen)) + "\n");
                        });

                        if(chatRoomNumber == 0){
                            historyWaitingRoom.add(message.substring(2, 2 + Integer.parseInt(nickLen)) + " : \n");
                            historyWaitingRoom.add("\t\t" + message.substring(Integer.parseInt(nickLen) + 5, Integer.parseInt(nickLen) + 5 + Integer.parseInt(messLen)) + "\n");
                        }
                        if(chatRoomNumber == 1){
                            historyFirstRoom.add(message.substring(2, 2 + Integer.parseInt(nickLen)) + " : \n");
                            historyFirstRoom.add("\t\t" + message.substring(Integer.parseInt(nickLen) + 5, Integer.parseInt(nickLen) + 5 + Integer.parseInt(messLen)) + "\n");
                        }
                        if(chatRoomNumber == 2){
                            historySecondRoom.add(message.substring(2, 2 + Integer.parseInt(nickLen)) + " : \n");
                            historySecondRoom.add("\t\t" + message.substring(Integer.parseInt(nickLen) + 5, Integer.parseInt(nickLen) + 5 + Integer.parseInt(messLen)) + "\n");
                        }
                        if(chatRoomNumber == 3){
                            historyThirdRoom.add(message.substring(2, 2 + Integer.parseInt(nickLen)) + " : \n");
                            historyThirdRoom.add("\t\t" + message.substring(Integer.parseInt(nickLen) + 5, Integer.parseInt(nickLen) + 5 + Integer.parseInt(messLen)) + "\n");
                        }
                        if(chatRoomNumber == 4){
                            historyFourthRoom.add(message.substring(2, 2 + Integer.parseInt(nickLen)) + " : \n");
                            historyFourthRoom.add("\t\t" + message.substring(Integer.parseInt(nickLen) + 5, Integer.parseInt(nickLen) + 5 + Integer.parseInt(messLen)) + "\n");
                        }
                        if(chatRoomNumber == 5){
                            historyFifthRoom.add(message.substring(2, 2 + Integer.parseInt(nickLen)) + " : \n");
                            historyFifthRoom.add("\t\t" + message.substring(Integer.parseInt(nickLen) + 5, Integer.parseInt(nickLen) + 5 + Integer.parseInt(messLen)) + "\n");
                        }

                        messageWriting.clear();
                    }

                    //Wyświetlanie informacji o stworzeniu pokoju i nadanie nazwy
                    else if(userAddRoom.equals(message.substring(0,5))){
                        message = message.substring(5, message.length());
                        roomNumber = message.substring(0, 1);
                        NewRoomLen = message.substring(1, 3);
                        NewRoomLen = NewRoomLen.replaceAll("^0*", "");
                        String nameRoom = message.substring(3, 3 + Integer.parseInt(NewRoomLen));
                        if(Integer.parseInt(roomNumber) == 4){
                            isFourthRoomAdded = true;

                            //Modyfikacja nazwy przycisku odpalonym na innym wątku
                            Platform.runLater(() -> {
                                messageDisplay.appendText("\t\t\t\t\t\t\t\t" + "Room " + nameRoom + " has been created\n");
                                fourth.setText(nameRoom);
                                if(chatRoomNumber == 4){
                                    messageWriting.setDisable(false);
                                    roomName.setDisable(true);
                                    newRoomButton.setDisable(true);
                                    deleteRoomButton.setDisable(false);
                                }
                            });



                        }
                        else if(Integer.parseInt(roomNumber) == 5){
                            isFifthRoomAdded = true;

                            //Modyfikacja nazwy przycisku odpalonym na innym wątku
                            Platform.runLater(() -> {
                                messageDisplay.appendText("\t\t\t\t\t\t\t\t" + "Room: " + nameRoom + " has been created\n");
                                fifth.setText(nameRoom);
                                if(chatRoomNumber == 5){
                                    messageWriting.setDisable(false);
                                    roomName.setDisable(true);
                                    newRoomButton.setDisable(true);
                                    deleteRoomButton.setDisable(false);
                                }
                            });


                        }

                    }

                    //Wyświetlanie informacji o usunięciu pokoju
                    else if(userDeleteRoom.equals(message.substring(0,5))){
                        message = message.substring(5, message.length());
                        roomNumber = message.substring(0, 1);
                        NewRoomLen = message.substring(1, 3);
                        NewRoomLen = NewRoomLen.replaceAll("^0*", "");
                        String nameRoom = message.substring(3, 3 + Integer.parseInt(NewRoomLen));
                        if(Integer.parseInt(roomNumber) == 4){
                            isFourthRoomAdded = false;
                            historyFourthRoom.clear();

                            //Modyfikacja nazwy przycisku odpalonym na innym wątku
                            Platform.runLater(() -> {
                                messageDisplay.appendText("\t\t\t\t\t\t\t\t" + "Room has been deleted\n");
                                fourth.setText(nameRoom);
                                if(chatRoomNumber == 4){
                                    messageWriting.setDisable(true);
                                    roomName.setDisable(false);
                                    newRoomButton.setDisable(false);
                                    deleteRoomButton.setDisable(true);
                                }
                            });

                        }
                        else if(Integer.parseInt(roomNumber) == 5){
                            isFifthRoomAdded = false;
                            historyFifthRoom.clear();

                            //Modyfikacja nazwy przycisku odpalonym na innym wątku
                            Platform.runLater(() -> {
                                fifth.setText(nameRoom);
                                messageDisplay.appendText("\t\t\t\t\t\t\t\t" + "Room has been deleted\n");
                                if(chatRoomNumber == 5){
                                    messageWriting.setDisable(true);
                                    roomName.setDisable(false);
                                    newRoomButton.setDisable(false);
                                    deleteRoomButton.setDisable(true);
                                }
                            });

                        }

                    }

                    //Wyświetlanie informacji o zmianie pokoju
                    else if (userJoinToRoom.equals(message.substring(0,5))) {
                        if(chatRoomNumber == 4 && isFourthRoomAdded == false){
                            Platform.runLater(() -> {
                                messageDisplay.appendText("\t\t\t\t\t\t\t\t" + "You are in empty room slot\n");
                            });
                        }
                        else if (chatRoomNumber == 5 && isFifthRoomAdded == false){
                            Platform.runLater(() -> {
                                messageDisplay.appendText("\t\t\t\t\t\t\t\t" + "You are in empty room slot\n");
                            });
                        }
                        else {
                            //Wyświetlenie informacji o dołączeniu nowego użytkownika do pokoju
                            message = message.substring(5, message.length());
                            String finalMessage1 = message;
                            Platform.runLater(() -> {
                                messageDisplay.appendText("\t\t\t\t\t\t\t\t" + finalMessage1.substring(2, finalMessage1.length()) + " join to room\n");
                            });

                        }

                    }
                    else if (userLeaveRoom.equals(message.substring(0,5))) {

                        //Wyświetlenie informacji o opuszczeniu pokoju przez użytkownika
                        message = message.substring(5, message.length());
                        String finalMessage = message;
                        Platform.runLater(() -> {
                            messageDisplay.appendText("\t\t\t\t\t\t\t\t" + finalMessage.substring(2, finalMessage.length()) + " leave room\n");
                        });

                        messageWriting.clear();
                    }
                }
                catch (IOException e) {
                    END = true;
                }
                catch (NullPointerException nu) {

                    messageDisplay.setText("ERROR! Server error. Application will be turn off. Please run it again!");
                    messageDisplay.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
                    messageDisplay.setStyle("-fx-text-inner-color: red");

                    {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            ;
                        }
                        System.exit(0);
                    }
                }
            }
        });
        initialize.start();
    }

    //Operacje ustawiania kolorów i wartości głównego ekranu Chatu
    @FXML
    public void waitingRoom(ActionEvent actionEvent) {
        roomName.setDisable(true);
        newRoomButton.setDisable(true);
        deleteRoomButton.setDisable(true);
        messageWriting.setDisable(false);
        waitRoom.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thirdWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        if(chatRoomNumber !=0) {
            sendRoomToServer("0");
            StringBuilder builder = new StringBuilder();
            for (String value : historyWaitingRoom) {
                builder.append(value);
            }
            messageDisplay.appendText(builder.toString());
        }

    }

    @FXML
    public void firstSport(ActionEvent actionEvent) {

        roomName.setDisable(true);
        newRoomButton.setDisable(true);
        deleteRoomButton.setDisable(true);
        messageWriting.setDisable(false);
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thirdWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        if(chatRoomNumber !=1) {
            sendRoomToServer("1");
            StringBuilder builder = new StringBuilder();
            for (String value : historyFirstRoom) {
                builder.append(value);
            }
            messageDisplay.appendText(builder.toString());
        }

    }

    @FXML
    public void secondStudy(ActionEvent actionEvent) {

        messageWriting.setDisable(false);
        roomName.setDisable(true);
        newRoomButton.setDisable(true);
        deleteRoomButton.setDisable(true);
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thirdWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        if(chatRoomNumber !=2) {
            sendRoomToServer("2");
            StringBuilder builder = new StringBuilder();
            for (String value : historySecondRoom) {
                builder.append(value);
            }
            messageDisplay.appendText(builder.toString());
        }
    }

    @FXML
    public void thirdWork(ActionEvent actionEvent) {

        roomName.setDisable(true);
        newRoomButton.setDisable(true);
        deleteRoomButton.setDisable(true);
        messageWriting.setDisable(false);
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thirdWork.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        if(chatRoomNumber !=3) {
            sendRoomToServer("3");
            StringBuilder builder = new StringBuilder();
            for (String value : historyThirdRoom) {
                builder.append(value);
            }
            messageDisplay.appendText(builder.toString());
        }
    }

    @FXML
    public void fourth(ActionEvent actionEvent) {

        if(!isFourthRoomAdded) {
            messageWriting.setDisable(true);
            roomName.setDisable(false);
            newRoomButton.setDisable(false);
            deleteRoomButton.setDisable(true);
        }
        else {
            messageWriting.setDisable(false);
            roomName.setDisable(true);
            newRoomButton.setDisable(true);
            deleteRoomButton.setDisable(false);
        }
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thirdWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourth.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        if(chatRoomNumber !=4) {
            sendRoomToServer("4");
            StringBuilder builder = new StringBuilder();
            for (String value : historyFourthRoom) {
                builder.append(value);
            }
            messageDisplay.appendText(builder.toString());
        }
    }

    @FXML
    public void fifth(ActionEvent actionEvent) {

        if(!isFifthRoomAdded) {
            messageWriting.setDisable(true);
            roomName.setDisable(false);
            newRoomButton.setDisable(false);
            deleteRoomButton.setDisable(true);
        }
        else {
            messageWriting.setDisable(false);
            roomName.setDisable(true);
            newRoomButton.setDisable(true);
            deleteRoomButton.setDisable(false);
        }
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thirdWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        if(chatRoomNumber !=5) {
            sendRoomToServer("5");
            StringBuilder builder = new StringBuilder();
            for (String value : historyFifthRoom) {
                builder.append(value);
            }
            messageDisplay.appendText(builder.toString());
        }

    }

    @FXML void newroom(ActionEvent actionEvent){

        if(chatRoomNumber == 4 && isFourthRoomAdded == false){

            if(!roomName.getText().isEmpty()) {
                sendNewRoomToServer("4", roomName.getText());
                isFourthRoomAdded = true;
                messageWriting.setDisable(false);
                roomName.setDisable(true);
                newRoomButton.setDisable(true);
                deleteRoomButton.setDisable(false);
                roomName.clear();
            }
        }
        else if(chatRoomNumber == 5 && isFifthRoomAdded == false){
            if(!roomName.getText().isEmpty()) {
                isFifthRoomAdded = true;
                sendNewRoomToServer("5", roomName.getText());
                messageWriting.setDisable(false);
                roomName.setDisable(true);
                newRoomButton.setDisable(true);
                deleteRoomButton.setDisable(false);
                roomName.clear();
            }
        }


    }

    @FXML void deleteroom(ActionEvent actionEvent){

        if(chatRoomNumber ==4 && isFourthRoomAdded == true){

            isFourthRoomAdded = false;
            sendDeleteRoomToServer("4", "+");
            messageWriting.setDisable(true);
            roomName.setDisable(false);
            newRoomButton.setDisable(false);
            deleteRoomButton.setDisable(false);
            roomName.clear();
        }
        else if(chatRoomNumber == 5 && isFifthRoomAdded == true){

            isFifthRoomAdded = false;
            sendDeleteRoomToServer("5", "+");
            messageWriting.setDisable(true);
            roomName.setDisable(false);
            newRoomButton.setDisable(false);
            deleteRoomButton.setDisable(true);
            roomName.clear();
        }


    }

}
