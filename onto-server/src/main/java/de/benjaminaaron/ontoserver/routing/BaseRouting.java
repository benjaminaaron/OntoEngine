package de.benjaminaaron.ontoserver.routing;

import de.benjaminaaron.ontoserver.jena.JenaController;
import de.benjaminaaron.ontoserver.routing.websocket.messages.CommandMessage;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseRouting {

    @Autowired
    protected JenaController jenaController;

    protected void handleCommand(CommandMessage command) {
        System.out.println("handleCommand: " + command);
        // TODO
    }
}
