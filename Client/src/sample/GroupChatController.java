package sample;


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


import sample.ConnectionToServer;
import sample.ApiWidnowController;

import java.io.IOException;

import static sample.Main.END;

//Klasa obsługująca główne okno aplikacji IRC
public class GroupChatController {


    @FXML
    private Button waitRoom;
    @FXML
    private Button firstSport;
    @FXML
    private Button secondStudy;
    @FXML
    private Button thridWork;
    @FXML
    private Button fourtShopping;
    @FXML
    private Button fifth;
    @FXML
    private TextField messageWriting;
    @FXML
    private TextArea messageDisplay;


    private ConnectionToServer serwer = new ConnectionToServer();

    private int chatRoomNumber = 0;


    //Metoda powoduje wysłanie wiadomości przez użytkonika po wciśnięciu przycisku ENTER
    public void sendMessageWhenEnter(ActionEvent actionEvent) throws IOException{

        //Można wysłać tylko niepustą wiadomość
        if(!messageWriting.getText().isEmpty()) {

            //tworzenie nowego wątku, aby współbierznie wysyłać wiadomości
            Thread thread = new Thread(() -> {
                try {
                    serwer.sendMessage(messageWriting.getText());
                    //System.out.println(messageWriting.getText());
                    messageWriting.clear();
                } catch (IOException e) {
                    ;
                }
            });
            thread.start();
        }
    }

    //Metoda wysyłająca do serwera wiadomość o zmiane pokoju
    private void sendRoomToServer(String chat_number) {
        messageDisplay.clear();
        messageWriting.clear();
        chatRoomNumber = Integer.parseInt(chat_number);
        if (!chat_number.equals("0"))
            messageWriting.setDisable(false);

        //tworzenie nowego wątku w celu wpółbierznośći w słaniu zapytań o zmianę pokoju
        Thread thread = new Thread(() -> {
            try {
                serwer.roomChange("cRoom" + chat_number);
            } catch (IOException e) {
                ;
            }
        });
        thread.start();
    }

    //Metoda inicializująca dla javaFX
    public void initialize(){

        //Nowy wątek w celu współbierzności, który działa aż do zakończenia programu
        Thread thread = new Thread(() -> {
            String nickLen;
            String messLen;
            String message;

            //Opcje Komunikatów od serwera
            String messageFromOtherUserInRoom = "messU";
            String userJoinToRoom  = "roomJ";
            String userLeaveRoom = "roomL";

            messageWriting.setDisable(true);
            messageDisplay.setWrapText(true);

            messageDisplay.setText("\t\t\t\t\t\tWitamy w komunikatorze IRC\n" +
            "\t\t\t\t\t\t\t\tZasady:\n" +
            "\t\t\t\t\t\tProszę nie pisac polskich znakow\n" +
            "\t\t\t\t\t\tTODO dodanie tworzenia nowych pokoji na pustych slotach\n");
            while (!END) {
                try {

                    message = serwer.messageGetFromServer();

                    //odzielenie wiadomości od serwera na nick wysyłającego i treść wiadomości do chatu
                    if (messageFromOtherUserInRoom.equals(message.substring(0,5)) && chatRoomNumber != 0) {

                        message = message.substring(5, message.length());
                        nickLen = message.substring(0, 2);
                        nickLen = nickLen.replaceAll("^0*", "");
                        messLen = message.substring(Integer.parseInt(nickLen) + 2, Integer.parseInt(nickLen) + 5);
                        messLen = messLen.replaceAll("^0*", "");

                        //wyświetlenie nicku i wiadomości na ekranie
                        messageDisplay.appendText(message.substring(2, 2 + Integer.parseInt(nickLen)) + " : \n");
                        messageDisplay.appendText("\t\t" + message.substring(Integer.parseInt(nickLen) + 5, Integer.parseInt(nickLen) + 5 + Integer.parseInt(messLen)) + "\n");
                    }
                    else if (userJoinToRoom.equals(message.substring(0,5)) && chatRoomNumber != 0) {

                        //Wyświetlenie informacji o dołączeniu nowego użytkownika do pokoju
                        message = message.substring(5, message.length());
                        messageDisplay.appendText("\t\t\t\t\t\t\t\t" + message.substring(2, message.length()) + " dołączył do pokoju.\n");
                    }
                    else if (userLeaveRoom.equals(message.substring(0,5)) && chatRoomNumber != 0) {

                        //Wyświetlenie informacji o opuszczeniu pokoju przez użytkownika
                        message = message.substring(5, message.length());
                        messageDisplay.appendText("\t\t\t\t\t\t\t\t" + message.substring(2, message.length()) + " opuścił pokój.\n");
                    }
                }
                catch (IOException e) {
                    END = true;
                }
                catch (NullPointerException nu) {

                    messageDisplay.setText("ERROR! Błąd po stronie serwera. Aplikacja zostanie wyłączona. Proszę uruchomić aplikację ponownie!");
                    messageDisplay.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
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
        thread.start();
    }

    //Operacje ustawiania kolorów i wartości głównego ekranu Chatu
    @FXML
    public void waitingRoom(ActionEvent actionEvent) {
        messageDisplay.clear();
        messageWriting.clear();
        messageWriting.setDisable(true);
        waitRoom.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        if(chatRoomNumber !=0) sendRoomToServer("0");
    }

    @FXML
    public void firstSport(ActionEvent actionEvent) {
        if(chatRoomNumber !=1) sendRoomToServer("1");
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
    }

    @FXML
    public void secondStudy(ActionEvent actionEvent) {
        if(chatRoomNumber !=2) sendRoomToServer("2");
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
    }

    @FXML
    public void thridWork(ActionEvent actionEvent) {
        if(chatRoomNumber !=3) sendRoomToServer("3");
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
    }

    @FXML
    public void fourtShopping(ActionEvent actionEvent) {
        if(chatRoomNumber !=4) sendRoomToServer("4");
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
    }

    @FXML
    public void fifth(ActionEvent actionEvent) {
        if(chatRoomNumber !=5) sendRoomToServer("5");
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifth.setText("IT");
    }
}
