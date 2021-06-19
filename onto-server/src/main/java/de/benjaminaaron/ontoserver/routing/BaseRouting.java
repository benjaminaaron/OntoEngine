package de.benjaminaaron.ontoserver.routing;

import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.model.io.Exporter;
import de.benjaminaaron.ontoserver.model.io.Importer;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.CommandMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseRouting {

    @Autowired
    protected ModelController modelController;
    @Autowired
    private Importer importer;
    @Autowired
    private Exporter exporter;

    protected AddStatementResponse addStatement(AddStatementMessage statementMsg) {
        return modelController.addStatement(statementMsg);
    }

    protected String addStatementStringResponse(String subject, String predicate, String object, boolean objectIsLiteral) {
        AddStatementMessage message = new AddStatementMessage();
        message.setSubject(subject);
        message.setPredicate(predicate);
        message.setObject(object);
        message.setObjectIsLiteral(objectIsLiteral);
        return addStatement(message).toString();
    }

    protected String handleCommand(CommandMessage commandMessage) {
        List<String> args = new LinkedList<>(Arrays.asList(commandMessage.getCommand().split(" ")));
        String command = args.remove(0).toLowerCase();
        switch (command) {
            case "print":
                modelController.printStatements();
                break;
            case "export":
                String arg0 = args.get(0).toLowerCase();
                if (arg0.equals("rdf")) {
                    exporter.exportRDF();
                }
                if (arg0.equals("graphml")) {
                    exporter.exportGraphml(args.size() >= 2 && args.get(1).equalsIgnoreCase("full"));
                }
                if (arg0.equals("graphdb")) {
                    exporter.exportToGraphDB();
                }
                break;
            case "import":
                if (args.get(0).equalsIgnoreCase("graphdb")) {
                    importer.importFromGraphDB(args.get(1));
                }
                break;
            case "replace":
                if (!args.get(1).equalsIgnoreCase("with")) {
                    return "no 'WITH' found";
                }
                modelController.replaceUris(Arrays.asList(args.get(0).split(",")), args.get(2));
                break;
            default:
                break;
        }
        return null;
    }
}
