package de.benjaminaaron.ontoserver.routing;

import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.routing.websocket.messages.CommandMessage;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseRouting {

    @Autowired
    protected ModelController modelController;

    protected String addStatement(String subject, String predicate, String object) {
        if (modelController.addStatement(subject, predicate, object)) {
            return "Statement added";
        }
        return "Statement already exists";
    }

    protected void handleCommand(CommandMessage command) {
        String commandStr = command.getCommand().split(" ")[0];
        String argsStr = command.getCommand().substring(commandStr.length() + 1);
        System.out.println("handleCommand: \"" + commandStr + "\", args \"" + argsStr + "\"");
        switch (commandStr) {
            case "print":
                modelController.printStatements();
                break;
            case "export":
                modelController.exportToRdfFile();
            default:
                break;
        }
    }
}
