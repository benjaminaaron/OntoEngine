package de.benjaminaaron.ontoserver.routing;

import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.routing.websocket.messages.CommandMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
        List<String> args = new LinkedList<>(Arrays.asList(command.getCommand().split(" ")));
        String commandStr = args.remove(0);
        System.out.println("handleCommand: \"" + commandStr + "\", args \"" + args + "\"");
        switch (commandStr) {
            case "print":
                modelController.printStatements();
                break;
            case "export":
                String arg0 = args.get(0).toLowerCase();
                if (arg0.equals("rdf")) {
                    modelController.exportRDF();
                }
                if (arg0.equals("graphml")) {
                    modelController.exportGraphml(args.size() >= 2 && args.get(1).equalsIgnoreCase("full"));
                }
            default:
                break;
        }
    }
}
