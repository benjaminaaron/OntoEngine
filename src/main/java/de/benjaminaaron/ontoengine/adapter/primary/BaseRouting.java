package de.benjaminaaron.ontoengine.adapter.primary;

import de.benjaminaaron.ontoengine.domain.exporter.GraphDbExporter;
import de.benjaminaaron.ontoengine.domain.exporter.GraphmlExporter;
import de.benjaminaaron.ontoengine.domain.exporter.MarkdownExporter;
import de.benjaminaaron.ontoengine.domain.exporter.RdfExporter;
import de.benjaminaaron.ontoengine.domain.importer.GraphDbImporter;
import de.benjaminaaron.ontoengine.domain.importer.MarkdownImporter;
import de.benjaminaaron.ontoengine.domain.importer.RdfImporter;
import de.benjaminaaron.ontoengine.domain.ModelController;
import de.benjaminaaron.ontoengine.domain.Utils;
import de.benjaminaaron.ontoengine.adapter.primary.messages.AddStatementMessage;
import de.benjaminaaron.ontoengine.adapter.primary.messages.AddStatementResponse;
import de.benjaminaaron.ontoengine.domain.importer.TgfImporter;
import de.benjaminaaron.ontoengine.domain.suggestion.SuggestionEngine;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseRouting {

    private final Logger logger = LogManager.getLogger(BaseRouting.class);

    @Autowired
    protected ModelController modelController;
    @Autowired
    protected SuggestionEngine suggestionEngine;

    public AddStatementResponse addStatement(AddStatementMessage statementMsg) {
        return modelController.addStatement(statementMsg, true);
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

    public String handleCommand(String commandStr) {
        List<String> args = new ArrayList<>(Arrays.asList(commandStr.split(" ")));
        String command = args.remove(0).toLowerCase();
        String format;
        boolean noKnownCommand = false;
        switch (command) {
            case "print":
                modelController.printStatements();
                break;
            case "export":
                format = args.get(0).toLowerCase(); // rdf, graphml or graphdb
                if (format.equals("markdown")) {
                    MarkdownExporter.export(modelController, args.get(1));
                    break;
                }
                String modelName = args.get(1).toLowerCase(); // main or meta
                if (format.equals("rdf")) {
                    return RdfExporter.export(modelController, modelName).toString();
                }
                if (format.equals("graphml")) {
                    GraphmlExporter.export(modelController, args.size() >= 3 && args.get(2).equalsIgnoreCase("full"));
                }
                if (format.equals("graphdb")) {
                    String ruleset = args.size() >= 3 ? args.get(2) : "empty"; // rdfsplus-optimized
                    GraphDbExporter.export(modelController, modelName, ruleset);
                }
                break;
            case "import":
                format = args.get(0).toLowerCase();
                if (format.equals("graphdb")) {
                    GraphDbImporter.doImport(modelController, args.get(1)); // repo
                }
                if (format.equals("markdown")) {
                    MarkdownImporter.doImport(modelController, args.get(1)); // folder name
                }
                if (format.equals("rdf") || format.equals("ttl")) {
                    RdfImporter.doImport(modelController, args.get(1)); // path to file
                }
                if (format.equals("tgf")) {
                    TgfImporter.doImport(modelController, args.get(1)); // path to file
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
            case "+":
                addStatement(args);
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
            case "query":
                String wherePart = commandStr.substring(6);
                return modelController.runSelectQueryUsingWherePart(wherePart);
            case "clear":
                if (args.get(0).equalsIgnoreCase("all")) {
                    modelController.clearAll();
                }
                break;
            case "statistics":
                return modelController.generateStatistics();
            default:
                noKnownCommand = true;
                break;
        }
        if (!noKnownCommand) {
            return null;
        }
        // if no known command is given, we see if it's a triple to be added
        args = new ArrayList<>(Arrays.asList(commandStr.split(" ")));
        if (args.size() == 3) {
            addStatement(args);
            return null;
        } else {
            logger.warn("Unknown command: " + commandStr);
            return "Unknown command";
        }
    }

    private void addStatement(List<String> args) {
        AddStatementMessage msg = new AddStatementMessage();
        msg.setSubject(args.get(0));
        msg.setPredicate(args.get(1));
        String obj = args.get(2);
        msg.setObjectIsLiteral(obj.startsWith("\"") && obj.endsWith("\""));
        msg.setObject(msg.isObjectIsLiteral() ? obj.substring(1, obj.length() - 1) : obj);
        modelController.addStatement(msg, true);
    }

    public JsonObject importUploadedFile(String fileName, InputStream inputStream) {
        return RdfImporter.doImportFromInputStream(modelController, fileName, inputStream);
    }

    public JsonObject runSelectQuery(String query) {
        return modelController.runCkgSelectQuery(query);
    }

    public JsonObject handleFormWorkflowTurtleFile(InputStream inputStream) {
        return modelController.handleFormWorkflowTurtleFile(inputStream);
    }

    public boolean addNewStatement(String sub, String pred, String obj) {
        return modelController.addNewStatement(sub, pred, obj);
    }

    public JsonObject getAllTriples() {
        return modelController.getAllTriples();
    }
}
