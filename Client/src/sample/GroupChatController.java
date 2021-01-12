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
    private Button fifthIT;


    private ConnectionToServer server = new ConnectionToServer();



    //Ustawianie kolorów i wartości głównego ekranu w zależności od pokoju w którym się aktualnie znajduje
    @FXML
    public void waitingRoom(ActionEvent actionEvent) {

        waitRoom.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifthIT.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
    }

    @FXML
    public void firstSport(ActionEvent actionEvent) {

        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifthIT.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
    }

    @FXML
    public void secondStudy(ActionEvent actionEvent) {
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifthIT.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
    }

    @FXML
    public void thridWork(ActionEvent actionEvent) {
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifthIT.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
    }

    @FXML
    public void fourtShopping(ActionEvent actionEvent) {
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifthIT.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
    }

    @FXML
    public void fifthIT(ActionEvent actionEvent) {
        waitRoom.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        firstSport.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        secondStudy.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        thridWork.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fourtShopping.setStyle("-fx-background-color: #c3c4c4; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
        fifthIT.setStyle("-fx-background-color: #f9aa33; -fx-background-insets: 0,1,4,5,6; -fx-background-radius: 9,8,5,4,3;");
    }
}
