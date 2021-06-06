package de.benjaminaaron.ontoclientjavafx.gui;

import de.benjaminaaron.ontoclientjavafx.websocket.WebSocketController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
    public CheckBox literalCheckBox;

    @FXML
    public void initialize() {}

    @FXML
    public void submitClicked() {
        webSocketController.sendAddStatement(
                subjectTextField.getText(), predicateTextField.getText(), objectTextField.getText(),
                literalCheckBox.isSelected());
    }

    @FXML
    public void sendCommandClicked() {
        webSocketController.sendCommand(commandTextField.getText());
    }
}
