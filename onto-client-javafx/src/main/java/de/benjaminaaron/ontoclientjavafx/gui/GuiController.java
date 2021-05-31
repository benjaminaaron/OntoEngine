package de.benjaminaaron.ontoclientjavafx.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.springframework.stereotype.Component;

@Component
public class GuiController {
    @FXML
    public void initialize() {}

    @FXML
    public void submitClicked(ActionEvent actionEvent) {
        System.out.println("submit clicked");
    }
}
