package de.benjaminaaron.ontoclientjavafx.gui;

import de.benjaminaaron.ontoclientjavafx.websocket.WebSocketController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuiController {

    @Autowired
    WebSocketController webSocketController;

    @FXML
    public void initialize() {}

    @FXML
    public void submitClicked(ActionEvent actionEvent) {
        System.out.println("submit clicked");
        webSocketController.sendAddStatement("javafx", "says", "hi");
    }
}
