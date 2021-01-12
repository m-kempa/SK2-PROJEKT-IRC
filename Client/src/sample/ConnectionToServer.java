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
        } catch (SocketTimeoutException ex) {
            window.ErrorWindow("ERROR", "Zbyt długi czas oczekiwania", "Sprawdź poprawność operacji łączenia z serwerem", Alert.AlertType.ERROR);
            userSocket.close();
            return false;
        } catch (IOException e) {
            window.ErrorWindow("ERROR", "Połączenie z serwerem nie powiodło się", "Sprawdź poprawność operacji łączenia z serwerem", Alert.AlertType.ERROR);
            userSocket.close();
            return false;
        } catch (Exception e) {
            window.ErrorWindow("ERROR", "Połączenie z serwerem nie powiodło się", "Sprawdź poprawność operacji łączenia z serwerem", Alert.AlertType.ERROR);
            userSocket.close();
            return false;
        }
        return true;
    }

    //Motoda wysyła do serwera nick użytkownika, serwer sprawdza unikalność, jeżeli użytkonik po podanym nicku już istnieje, serwer wysyła komunikat
    public boolean IsNickUnique(String nick, ApiWidnowController window) throws IOException {

        //Nie można zalogować się bez nicku
        if (nick.replaceAll(" ", "").isEmpty()) {
            window.ErrorWindow( "ERROR","Brak wypisanego pola Nick", "Pole Nick musi być wypełnione!", Alert.AlertType.ERROR);
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
                window.ErrorWindow("ERROR", "Zbyt duża ilość użytkowników", "Proszę poczekać", Alert.AlertType.ERROR);
                userSocket.close();
                return false;
            }
            else {
                window.ErrorWindow("ERROR", "Podany Nick już istnieje", "Proszę wprowadzić inny Nick", Alert.AlertType.ERROR);
                userSocket.close();
                return false;
            }
        } catch (Exception in) {
            window.ErrorWindow("ERROR", "Próba wysłania danych do serwera nie powiodła się.", "Spróboj ponownie, może serwer jest niedostępny.", Alert.AlertType.ERROR);
            userSocket.close();
            return false;
        }
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
