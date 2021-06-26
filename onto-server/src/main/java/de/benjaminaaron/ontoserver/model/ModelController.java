package de.benjaminaaron.ontoserver.model;

import de.benjaminaaron.ontoserver.model.MetaHandler.StatementOrigin;
import de.benjaminaaron.ontoserver.model.graph.Graph;
import de.benjaminaaron.ontoserver.routing.websocket.WebSocketRouting;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementResponse;
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
    @Value("${jena.tdb.model.main.name}")
    private String MAIN_MODEL_NAME;
    @Value("${jena.tdb.model.meta.name}")
    private String META_MODEL_NAME;
    @Autowired
    private WebSocketRouting router;
    private Model mainModel, metaModel;
    private Graph graph;
    private MetaHandler metaHandler;

    @Value("${uri.default.namespace}")
    public void setUriDefaultNamespace(String ns) {
        Utils.DEFAULT_URI_NAMESPACE = ns;
    }

    @Value("${uri.default.separator}")
    public void setUriDefaultSeparator(String sep) {
        Utils.DEFAULT_URI_SEPARATOR = sep;
    }

    @Value("classpath:meta.owl")
    private Path META_OWL;

    @PostConstruct
    private void init() {
        Dataset dataset = TDBFactory.createDataset(TBD_DIR.toString()) ;
        if (!dataset.containsNamedModel(MAIN_MODEL_NAME)) {
            logger.info("Creating " + MAIN_MODEL_NAME + "-model in TDB location " + TBD_DIR);
        }
        mainModel = dataset.getNamedModel(MAIN_MODEL_NAME);
        boolean metaModelInitialized = dataset.containsNamedModel(META_MODEL_NAME);
        metaModel = dataset.getNamedModel(META_MODEL_NAME);
        if (!metaModelInitialized) {
            metaModel.setNsPrefix("meta", MetaHandler.META_NS + "#");
            logger.info("Creating " + META_MODEL_NAME + "-model in TDB location " + TBD_DIR);
        }
        metaHandler = new MetaHandler(mainModel, metaModel, META_OWL);
        graph = new Graph(mainModel);
        printStatements();
    }

    @PreDestroy
    private void close() {
        mainModel.close();
        metaModel.close();
    }

    public AddStatementResponse addStatement(AddStatementMessage statementMsg) {
        Resource sub = mainModel.createResource(ensureUri(statementMsg.getSubject()));
        Property pred = mainModel.createProperty(ensureUri(statementMsg.getPredicate()));
        RDFNode obj;
        if (statementMsg.isObjectIsLiteral()) {
            obj = mainModel.createTypedLiteral(detectLiteralType(statementMsg.getObject()));
        } else {
            obj = mainModel.createResource(ensureUri(statementMsg.getObject()));
        }
        Statement statement = ResourceFactory.createStatement(sub, pred, obj);
        AddStatementResponse response = new AddStatementResponse();
        if (mainModel.contains(statement)) {
            return response;
        }
        addStatement(statement, StatementOrigin.ADD, "client", response);
        return response;
    }

    public void addStatement(Statement statement, StatementOrigin origin, String info, AddStatementResponse response) {
        metaHandler.storeNewTripleEvent(statement, origin, info, response);
        mainModel.add(statement);
        graph.importStatement(statement);
    }

    public void replaceUris(Set<String> from, String to) {
        List<Statement> deletionList = new ArrayList<>();
        List<Statement> insertionList = new ArrayList<>();
        int replaceCount = 0;
        StmtIterator iter = mainModel.listStatements();
        while (iter.hasNext()) {
            Statement statement = iter.nextStatement();
            boolean replaceSubject = from.contains(statement.getSubject().getURI());
            boolean replacePredicate = from.contains(statement.getPredicate().getURI());
            boolean replaceObject = statement.getObject().isResource() && from.contains(statement.getObject().asResource().getURI());
            if (replaceSubject || replacePredicate || replaceObject) {
                deletionList.add(statement);
                Resource sub = replaceSubject ? mainModel.createResource(to) : statement.getSubject();
                Property pred = replacePredicate ? mainModel.createProperty(to) : statement.getPredicate();
                RDFNode obj = replaceObject ? mainModel.createResource(to) : statement.getObject();
                insertionList.add(ResourceFactory.createStatement(sub, pred, obj));
                replaceCount += (replaceSubject ? 1 : 0) + (replacePredicate ? 1 : 0) + (replaceObject ? 1 : 0);
            }
        }
        assert deletionList.size() == insertionList.size();
        mainModel.remove(deletionList);
        mainModel.add(insertionList);
        router.sendMessage(replaceCount + " URIs in " + insertionList.size() + " statements replaced");
    }

    public Model getMainModel() {
        return mainModel;
    }

    public MetaHandler getMetaHandler() {
        return metaHandler;
    }

    public Graph getGraph() {
        return graph;
    }

    public void printStatements() {
        mainModel.listStatements().toList().forEach(System.out::println);
    }
}
