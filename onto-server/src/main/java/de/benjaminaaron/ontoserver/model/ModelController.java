package de.benjaminaaron.ontoserver.model;

import de.benjaminaaron.ontoserver.model.graph.Graph;
import de.benjaminaaron.ontoserver.routing.websocket.WebSocketRouting;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementResponse;
import de.benjaminaaron.ontoserver.suggestion.SuggestionEngine;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static de.benjaminaaron.ontoserver.model.Utils.detectLiteralType;
import static de.benjaminaaron.ontoserver.model.Utils.ensureUri;

@Component
public class ModelController {

    private final Logger logger = LogManager.getLogger(ModelController.class);

    @Value("${jena.tdb.directory}")
    private Path TBD_DIR;

    @Autowired
    private WebSocketRouting router;
    @Autowired
    private SuggestionEngine suggestionEngine;
    private Model model;
    private Graph graph;

    @PostConstruct
    private void init() {
        Dataset dataset = TDBFactory.createDataset(TBD_DIR.toString()) ;
        model = dataset.getDefaultModel();
        // graph = new Graph(model);
        printStatements();
    }

    @PreDestroy
    private void close() {
        model.close();
    }

    public AddStatementResponse addStatement(AddStatementMessage statementMsg) {
        Resource sub = model.createResource(ensureUri(statementMsg.getSubject()));
        Property pred = model.createProperty(ensureUri(statementMsg.getPredicate()));
        RDFNode obj;
        if (statementMsg.isObjectIsLiteral()) {
            obj = model.createTypedLiteral(detectLiteralType(statementMsg.getObject()));
        } else {
            obj = model.createResource(ensureUri(statementMsg.getObject()));
        }
        Statement statement = ResourceFactory.createStatement(sub, pred, obj);
        AddStatementResponse response = new AddStatementResponse();
        if (model.contains(statement)) {
            return response;
        }
        response.setStatementAdded(true);
        response.setSubjectIsNew(!model.getGraph().contains(sub.asNode(), Node.ANY, Node.ANY));
        response.setPredicateIsNew(!model.getGraph().contains(Node.ANY, pred.asNode(), Node.ANY));
        response.setObjectIsNew(!model.getGraph().contains(Node.ANY, Node.ANY, obj.asNode()));
        addStatement(statement);
        // CompletableFuture.runAsync(() -> func());
        return response;
    }

    public void addStatement(Statement statement) {
        logger.info("Statement added: " + statement.getSubject() + ", " + statement.getPredicate() + ", " + statement.getObject());
        model.add(statement);
        // graph.importStatement(statement);
    }

    public void replaceUris(Set<String> from, String to) {
    }

    public Model getModel() {
        return model;
    }

    public Graph getGraph() {
        return graph;
    }

    public void printStatements() {
        model.listStatements().toList().forEach(System.out::println);
    }
}
