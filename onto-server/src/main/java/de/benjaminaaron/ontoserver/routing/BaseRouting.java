package de.benjaminaaron.ontoserver.routing;

import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.model.Utils;
import de.benjaminaaron.ontoserver.model.io.Exporter;
import de.benjaminaaron.ontoserver.model.io.Importer;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementResponse;
import de.benjaminaaron.ontoserver.routing.websocket.messages.CommandMessage;
import de.benjaminaaron.ontoserver.suggestion.SuggestionEngine;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class BaseRouting {

    @Autowired
    protected ModelController modelController;
    @Autowired
    protected SuggestionEngine suggestionEngine;
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
        return handleCommand(commandMessage.getCommand());
    }

    protected String handleCommand(String commandStr) {
        List<String> args = new ArrayList<>(Arrays.asList(commandStr.split(" ")));
        String command = args.remove(0).toLowerCase();
        switch (command) {
            case "print":
                modelController.printStatements();
                break;
            case "export":
                String arg0 = args.get(0).toLowerCase();
                if (arg0.equals("rdf")) {
                    if (args.size() > 1 && args.get(1).equalsIgnoreCase("meta")) {
                        exporter.exportRDF("meta");
                    } else {
                        exporter.exportRDF("main");
                    }
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
                    return "no 'WITH' keyword found";
                }
                Set<String> from = Set.of(args.get(0).split(","));
                String to = args.get(2);
                if (!Utils.containsOnlyValidUris(from) || !Utils.isValidUri(to)) {
                    return "contains invalid URIs";
                }
                modelController.replaceUris(from, to);
                break;
            case "accept":
                String id = args.get(0);
                if (!suggestionEngine.suggestionExists(id)) {
                    return "no suggestion with id " + id + " found";
                }
                handleCommand(suggestionEngine.accept(id));
                break;
            default:
                return "Unknown command";
        }
        return null;
    }
}
