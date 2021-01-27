package sample;

import javafx.scene.control.Alert;
import javafx.scene.text.Font;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.*;

//Ta klasa obsługuję komunikację między kliente i serwerem
public class ConnectionToServer {

    private static int port;
    private static String ip_host;

    private static Socket userSocket = null;


    //Metoda próbuje nawiązać połączenie z serwerem i w przypadku nieudanej próby, wyświetla odpowiedni komunikat
    public boolean serverConnection(String ip_host, int port, ApiWidnowController window) throws IOException {

        ConnectionToServer.port = port;
        ConnectionToServer.ip_host = ip_host;
        SocketAddress socketAddress = new InetSocketAddress(ip_host, port);
        userSocket = new Socket();


        //ograniczenie czasowe w którym musi nastąpić połączenie z serwerem, w razie przekroczenie zwracany jest błąd
        int connection_limit = 1500;

        //Blok obsługi błędów połączenia
        try {
            userSocket.connect(socketAddress, connection_limit);
        }
        catch (SocketTimeoutException ex) {
            window.ErrorWindow("ERROR", "To long waiting time", "Validate the connection to the server operation", Alert.AlertType.ERROR);
            userSocket.close();
            return false;
        }
        catch (IOException e) {
            window.ErrorWindow("ERROR", "The connection to the server has failed", "Validate the connection to the server operation", Alert.AlertType.ERROR);
            userSocket.close();
            return false;
        }
        catch (Exception e) {
            window.ErrorWindow("ERROR", "The connection to the server has failed", "Validate the connection to the server operation", Alert.AlertType.ERROR);
            userSocket.close();
            return false;
        }
        return true;
    }

    //Motoda wysyła do serwera nick użytkownika, serwer sprawdza unikalność, jeżeli użytkonik po podanym nicku już istnieje, serwer wysyła komunikat
    public boolean IsNickUnique(String nick, ApiWidnowController window) throws IOException {

        //Nie można zalogować się bez nicku
        if (nick.replaceAll(" ", "").isEmpty()) {
            window.ErrorWindow( "ERROR","Nick field is empty", "Nick must be non empty!", Alert.AlertType.ERROR);
            return false;
        }

        //Podpatrzone z materiałów z labo
        PrintWriter writer = new PrintWriter(userSocket.getOutputStream(), true);


        String length = "";
        length = NickZeroAdd(nick);

        //Wysłanie komunikatu do serwera, kótry zawiera: login + długość nicku + nick użytkownika
        writer.println("login" + length + nick);

        //W bloku try cath otrzymujemy odpowiedź od serwera, który sprawdza unikalność nicku, w razie braku unikalności zostanie wyświetlony błąd
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            String serverMessage = reader.readLine();

            if (serverMessage.equals("0")) {
                return true;
            }
            else if(serverMessage.equals("1")){
                window.ErrorWindow("ERROR", "Too many users", "Please wait", Alert.AlertType.ERROR);
                userSocket.close();
                return false;
            }
            else {
                window.ErrorWindow("ERROR", "This nick name already exist", "Please input different nickname", Alert.AlertType.ERROR);
                userSocket.close();
                return false;
            }
        } catch (Exception in) {
            window.ErrorWindow("ERROR", "An attempt to send data to the server has failed.", "Try again, maybe the server is unavailable.", Alert.AlertType.ERROR);
            userSocket.close();
            return false;
        }
    }

    //Klient za pomocą tej metody wysyła informację o nadaniu nazwy nowemu pokojowi
    public void roomAdd(String roomName) throws IOException {
        PrintWriter writer = new PrintWriter(userSocket.getOutputStream(), true);
        writer.println(roomName);
        //System.out.println(roomName);
    }

    //Klient za pomocą tej metody wysyła informację o zmianie pokoju na inny
    public void roomChange(String roomNumber) throws IOException {
        PrintWriter writer = new PrintWriter(userSocket.getOutputStream(), true);
        writer.println(roomNumber);
    }

    //Metoda wysyłająca wiadomość do serwera od użytkownika piszącego wiadomość
    public void sendMessage(String message) throws IOException {

        PrintWriter writer = new PrintWriter(userSocket.getOutputStream(), true);
        String length = MessageZeroAdd(message);
        writer.println("mSend" + length + message);
    }



    //Metoda odbiera wiadomości od serwera (które wysłali inni użytkownicy)
    public String messageGetFromServer() throws IOException {


        String serverMessage = "XXXXXXXXXXXXXXXXX";
        String message;
        String nickLen;
        String messLen;
        String roomNumber;
        String NewRoomLen;

        //Opcje Komunikatów od serwera
        String messageFromOtherUserInRoom = "messU";
        String userJoinToRoom  = "roomJ";
        String userLeaveRoom = "roomL";
        String userAddRoom = "roomA";

        try {
            //zaporzyczone z szblonu z zajęc
            BufferedReader reader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            serverMessage = reader.readLine();


            //Opcja messU oznacza nową wiadomość od innych użytkoników znajdujących się w pokoju
            if (messageFromOtherUserInRoom.equals(serverMessage.substring(0,5))) {

                //odrzucamy pierwszy znak z kodem wiadomości
                serverMessage = serverMessage.substring(5, serverMessage.length());

                //odczytanie dł nicku
                nickLen = serverMessage.substring(0, 2);
                nickLen = nickLen.replaceAll("^0*", ""); //zamieniamy nieznaczących zer na puste pola

                //odczytanie długości wiadomości
                messLen = serverMessage.substring(Integer.parseInt(nickLen) + 2, Integer.parseInt(nickLen) + 5);
                messLen = messLen.replaceAll("^0*", "");

                message = serverMessage.substring(Integer.parseInt(nickLen) + 5, serverMessage.length());

                //Czytamy tak długo aż zapisze całą wiadomość o wcześniej pobranej długości
                while (message.length() < Integer.parseInt(messLen)) {
                    message = message + reader.readLine();
                }
                serverMessage = messageFromOtherUserInRoom + serverMessage.substring(0, 5 + Integer.parseInt(nickLen)) + message;

            }

            //Opcja roomA oznacza stworzenie przez użytkownika nowego pokoju na wolnym slocie
            else if (userAddRoom.equals(serverMessage.substring(0,5))) {
                serverMessage = serverMessage.substring(5, serverMessage.length());
                roomNumber = serverMessage.substring(0,1);
                NewRoomLen = serverMessage.substring(1, 3);
                NewRoomLen = NewRoomLen.replaceAll("^0*", "");
                message = serverMessage.substring(3, serverMessage.length());

                //Czytamy tak długo aż zapisze całą wiadomość o wcześniej pobranej długości
                while (message.length() < Integer.parseInt(NewRoomLen)) {
                    message = message + reader.readLine();
                }
                serverMessage = userAddRoom + serverMessage.substring(0, 3) + message;


            }

            //Opcja roomJ oznacza dołączenie nowego użytkownika do pokoju
            else if (userJoinToRoom.equals(serverMessage.substring(0,5))) {
                serverMessage = serverMessage.substring(5, serverMessage.length());
                nickLen = serverMessage.substring(0, 2);
                nickLen = nickLen.replaceAll("^0*", "");
                message = serverMessage.substring(2, serverMessage.length());

                //Czytamy tak długo aż zapisze całą wiadomość o wcześniej pobranej długości
                while (message.length() < Integer.parseInt(nickLen)) {
                    message = message + reader.readLine();
                }
                serverMessage = userJoinToRoom + serverMessage.substring(0, 2) + message;


            }

            //Opcja roomL oznacza opuszczenie pokoju przez użytkownika
            else if (userLeaveRoom.equals(serverMessage.substring(0,5))) {
                serverMessage = serverMessage.substring(5, serverMessage.length());
                nickLen = serverMessage.substring(0, 2);
                nickLen = nickLen.replaceAll("^0*", "");
                message = serverMessage.substring(2, serverMessage.length());

                //Czytamy tak długo aż zapisze całą wiadomość o wcześniej pobranej długości
                while (message.length() < Integer.parseInt(nickLen)) {
                    message = message + reader.readLine();
                }
                serverMessage = userLeaveRoom + serverMessage.substring(0, 2) + message;
            }
        } catch (Exception e) {
            ;
        }


        return serverMessage;
    }


    //Ta metoda zamyka połączenie z serwerem
    public void connectionClosed() throws IOException {
        try {
            PrintWriter writer = new PrintWriter(userSocket.getOutputStream(), true);
            writer.println("close");
            userSocket.close();
        } catch (Exception e) {
            ;
        }
    }

    // metoda służąca uzupełnieniu długości Wiadomości
    // dozwolona długość wiadomości od 1-999
    private String MessageZeroAdd(String login) {
        String length;
        if (login.length() < 10)
            length = "00" + Integer.toString(login.length());
        else if (login.length() < 100)
            length = "0" + Integer.toString(login.length());
        else
            length = Integer.toString(login.length());
        return length;
    }

    // metoda służąca uzupełnieniu długości nicku zerem jeżeli jego długość jest mnijsza do 10
    private String NickZeroAdd(String login) {
        String length;
        if (login.length() < 10)
            length = "0" + Integer.toString(login.length());
        else
            length = Integer.toString(login.length());
        return length;
    }


}
