package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;


//Klasa obsługująca okna aplikacji IRC
public class ApiWidnowController {


    //Metoda przełączająca z okna logowania do okna komunikatora
    public void switchToChatWindow(ActionEvent actionEvent, String path, int width, int height) throws IOException {
        Parent log_root = FXMLLoader.load(getClass().getResource(path));
        Scene scene = new Scene(log_root, width, height);
        Stage GroupChat = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        GroupChat.setTitle("IRC Communicator");
        GroupChat.setResizable(false);
        GroupChat.setScene(scene);

        GroupChat.show();
    }


    //Metoda wyświetlająca okienka błędów i ostrzeżeń w celu informacyjnym dla użytkownika
    @FXML
    public void ErrorWindow(String title, String header, String text, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);

        alert.showAndWait();
    }
}
