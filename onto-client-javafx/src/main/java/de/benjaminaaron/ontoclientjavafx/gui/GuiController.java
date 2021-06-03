package de.benjaminaaron.ontoclientjavafx.gui;

import de.benjaminaaron.ontoclientjavafx.websocket.WebSocketController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuiController {

    @Autowired
    WebSocketController webSocketController;
    public TextField subjectTextField;
    public TextField predicateTextField;
    public TextField objectTextField;
    public TextField commandTextField;

    @FXML
    public void initialize() {}

    @FXML
    public void submitClicked(ActionEvent actionEvent) {
        webSocketController.sendAddStatement(subjectTextField.getText(), predicateTextField.getText(), objectTextField.getText());
    }

    @FXML
    public void sendCommandClicked(ActionEvent actionEvent) {
        webSocketController.sendCommand(commandTextField.getText());
    }
}
