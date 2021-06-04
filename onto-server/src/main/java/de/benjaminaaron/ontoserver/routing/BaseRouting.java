package de.benjaminaaron.ontoserver.routing;

import de.benjaminaaron.ontoserver.jena.JenaController;
import de.benjaminaaron.ontoserver.routing.websocket.messages.CommandMessage;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseRouting {

    @Autowired
    protected JenaController jenaController;

    protected void handleCommand(CommandMessage command) {
        String commandStr = command.getCommand().split(" ")[0];
        String argsStr = command.getCommand().substring(commandStr.length() + 1);
        System.out.println("handleCommand: \"" + commandStr + "\", args \"" + argsStr + "\"");
        switch (commandStr) {
            case "print":
                jenaController.printStatements();
                break;
            case "export":
                jenaController.exportToRdfFile();
            default:
                break;
        }
    }
}
