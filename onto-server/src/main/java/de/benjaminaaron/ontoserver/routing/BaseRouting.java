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
import org.springframework.stereotype.Component;

@Component
public class BaseRouting {

    @Autowired
    protected ModelController modelController;
    @Autowired
    protected SuggestionEngine suggestionEngine;
    @Autowired
    private Importer importer;
    @Autowired
    private Exporter exporter;

    public AddStatementResponse addStatement(AddStatementMessage statementMsg) {
        return modelController.addStatement(statementMsg);
    }

    public String addStatementStringResponse(String subject, String predicate, String object,
        boolean objectIsLiteral) {
        AddStatementMessage message = new AddStatementMessage();
        message.setSubject(subject);
        message.setPredicate(predicate);
        message.setObject(object);
        message.setObjectIsLiteral(objectIsLiteral);
        return addStatement(message).toString();
    }

    public String handleCommand(CommandMessage commandMessage) {
        return handleCommand(commandMessage.getCommand());
    }

    protected String handleCommand(String commandStr) {
        List<String> args = new ArrayList<>(Arrays.asList(commandStr.split(" ")));
        String command = args.remove(0).toLowerCase();
        String format;
        switch (command) {
            case "print":
                modelController.printStatements();
                break;
            case "export":
                format = args.get(0).toLowerCase(); // rdf, graphml or graphdb
                if (format.equals("markdown")) {
                    exporter.exportMarkdown(args.get(1));
                    break;
                }
                String model = args.get(1).toLowerCase(); // main or meta
                if (format.equals("rdf")) {
                    exporter.exportRDF(model);
                }
                if (format.equals("graphml")) {
                    exporter.exportGraphml(args.size() >= 3 && args.get(2).equalsIgnoreCase("full"));
                }
                if (format.equals("graphdb")) {
                    String ruleset = args.size() >= 3 ? args.get(2) : "empty"; // rdfsplus-optimized
                    exporter.exportToGraphDB(model, ruleset);
                }
                break;
            case "import":
                format = args.get(0).toLowerCase();
                if (format.equals("graphdb")) {
                    importer.importFromGraphDB(args.get(1)); // repo
                }
                if (format.equals("markdown")) {
                    importer.importFromMarkdown(args.get(1)); // folder name
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
            case "add":
                AddStatementMessage msg = new AddStatementMessage();
                msg.setSubject(args.get(0));
                msg.setPredicate(args.get(1));
                msg.setObject(args.get(2));
                msg.setObjectIsLiteral(false); // pass as optional args.get(3) boolean? TODO
                modelController.addStatement(msg);
                break;
            case "accept":
                String id = args.get(0);
                if (!suggestionEngine.suggestionExists(id)) {
                    return "no suggestion with id " + id + " found";
                }
                String storedCommandStr = suggestionEngine.accept(id);
                for (String cmd : storedCommandStr.split(",")) {
                    handleCommand(cmd.trim());
                }
                break;
            case "dev":
                modelController.dev(args.size() > 1 ? commandStr.substring(4) : null);
                break;
            default:
                return "Unknown command";
        }
        return null;
    }
}
