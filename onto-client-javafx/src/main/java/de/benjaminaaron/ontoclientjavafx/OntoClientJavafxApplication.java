package de.benjaminaaron.ontoclientjavafx;

import de.benjaminaaron.ontoclientjavafx.gui.GuiApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OntoClientJavafxApplication {

    public static void main(String[] args) {
        Application.launch(GuiApplication.class, args);
    }

}
