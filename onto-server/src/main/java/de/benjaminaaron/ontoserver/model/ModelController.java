package de.benjaminaaron.ontoserver.model;

import de.benjaminaaron.ontoserver.model.graph.Graph;
import de.benjaminaaron.ontoserver.routing.websocket.WebSocketRouting;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementResponse;
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
import java.util.ArrayList;
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
    private Model model;
    private Graph graph;

    @Value("${uri.default.namespace}")
    public void setUriDefaultNamespace(String ns) {
        Utils.DEFAULT_URI_NAMESPACE = ns;
    }

    @Value("${uri.default.separator}")
    public void setUriDefaultSeparator(String sep) {
        Utils.DEFAULT_URI_SEPARATOR = sep;
    }

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
        List<Statement> deletionList = new ArrayList<>();
        List<Statement> insertionList = new ArrayList<>();
        int replaceCount = 0;
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement statement = iter.nextStatement();
            boolean replaceSubject = from.contains(statement.getSubject().getURI());
            boolean replacePredicate = from.contains(statement.getPredicate().getURI());
            boolean replaceObject = statement.getObject().isResource() && from.contains(statement.getObject().asResource().getURI());
            if (replaceSubject || replacePredicate || replaceObject) {
                deletionList.add(statement);
                Resource sub = replaceSubject ? model.createResource(to) : statement.getSubject();
                Property pred = replacePredicate ? model.createProperty(to) : statement.getPredicate();
                RDFNode obj = replaceObject ? model.createResource(to) : statement.getObject();
                insertionList.add(ResourceFactory.createStatement(sub, pred, obj));
                replaceCount += (replaceSubject ? 1 : 0) + (replacePredicate ? 1 : 0) + (replaceObject ? 1 : 0);
            }
        }
        assert deletionList.size() == insertionList.size();
        model.remove(deletionList);
        model.add(insertionList);
        router.sendMessage(replaceCount + " URIs in " + insertionList.size() + " statements replaced");
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
