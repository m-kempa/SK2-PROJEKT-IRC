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
                    //
                    CONNECTION_SUCCESS = server.serverConnection(ip.getText(), Integer.parseInt(port.getText()), window );
                }
                else{
                    window.ErrorWindow( "ERROR","Brak wypisanego pola Nick",
                            "Pole Nick musi być wypełnione!", Alert.AlertType.ERROR);
                }
            }
            catch (Exception e)
            {
                window.ErrorWindow( "ERROR","Wpisano zły numer portu",
                        "Port musi być liczbą!", Alert.AlertType.ERROR);
            }

        }
        //jeżeli połączenie zostało nawiązane
        if(CONNECTION_SUCCESS)
        {

                window.switchToChatWindow(actionEvent, "GroupChat.fxml", 1032, 736);

        }

    }

    //podstawowe wartości dla pól port i IP
    public void initialize()
    {
        port.setText("3333");
        ip.setText("127.0.0.1");
    }
}

