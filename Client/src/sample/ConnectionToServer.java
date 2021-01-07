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
            window.ErrorWindow("Błąd", "Zbyt długi czas oczekiwania",
                    "Sprawdź poprawność operacji", Alert.AlertType.ERROR);
            userSocket.close();
            return false;
        } catch (IOException e) {
            window.ErrorWindow("Błąd", "Nie udało się podłączyć do servera",
                    "Sprawdź poprawność operacji", Alert.AlertType.ERROR);
            userSocket.close();
            return false;
        } catch (Exception e) {
            window.ErrorWindow("Błąd", "Nie udało się podłączyć do servera",
                    "Sprawdź poprawność operacji", Alert.AlertType.ERROR);
            userSocket.close();
            return false;
        }
        return true;
    }






}
