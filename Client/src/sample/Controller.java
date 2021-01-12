package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import javafx.scene.control.TextField;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import sample.ConnectionToServer;
import sample.ApiWidnowController;


//Klasa okna logowania do komunikatora IRC
public class Controller {

    private boolean CONNECTION_SUCCESS = false;

    //Odwołanie do pól z pliku LoginPanel.fxml
    @FXML
    private TextField port;
    @FXML
    private TextField ip;
    @FXML
    private TextField nick;

    private ApiWidnowController window = new ApiWidnowController();

    private ConnectionToServer server;

    public Controller() throws IOException {
        server = new ConnectionToServer();
    }



    //Metoda logowania do komunikatora IRC
    @FXML
    public void logger(ActionEvent actionEvent) throws IOException {

        //Jeżeli użytkonik nie uzyskał jeszcze połączenia
        if(!CONNECTION_SUCCESS)
        {
            //jeśli login nie został wpisany, nie zostanie podjęta próba połączenia w celu ułatwienia pracy serwera
            try {
                Integer.parseInt(port.getText());
                //Pole nick jest wymagane, jeśli zostanie pominięte próba połączenia z serwerem się nie odbędzie i zostanie wyświetlony ERROR
                if(!nick.getText().isEmpty()) {
                    CONNECTION_SUCCESS = server.serverConnection(ip.getText(), Integer.parseInt(port.getText()), window );
                }
            }
            catch (Exception e)
            {
                window.ErrorWindow( "ERROR","Wpisano zły numer portu",
                        "Port musi być liczbą!", Alert.AlertType.ERROR);
            }

        }
        //jeżeli połączenie się powiodło i login przeszedł test unikalności przechodzimy do głównej storny chatu
        if(CONNECTION_SUCCESS)
        {
            if(server.IsNickUnique(nick.getText(), window))
            {
                window.switchToChatWindow(actionEvent, "GroupChat.fxml", 932, 636);
            }
            else
                CONNECTION_SUCCESS = false;
        }

    }

    //podstawowe wartości dla pól port i IP
    public void initialize()
    {
        port.setText("3333");
        ip.setText("127.0.0.1");
    }
}

