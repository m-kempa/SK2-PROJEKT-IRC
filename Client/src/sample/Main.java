package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


//Klasa główna inicująca okno logowania do systemu IRC
public class Main extends Application {


    private static ConnectionToServer serwer = new ConnectionToServer();
    public static boolean END = false;

    //Metoda która rozpoczyna api (okno startowe)
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("LoginPanel.fxml"));
        primaryStage.setTitle("IRC Panel Logowania");
        primaryStage.setScene(new Scene(root, 460, 296));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    //Metoda główna projektu, w niej w przyszłości będzie zamykane połączenie z serwerem
    public static void main(String[] args) throws IOException {
        launch(args);
        END = true;

        //Wysłanie do serwera wiadomości o wyłączeniu aplikacji przez klienta
        serwer.connectionClosed();
    }
}
