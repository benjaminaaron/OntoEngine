package de.benjaminaaron.ontoengine.model;

import static de.benjaminaaron.ontoengine.model.Utils.detectLiteralType;
import static de.benjaminaaron.ontoengine.model.Utils.ensureUri;

import de.benjaminaaron.ontoengine.model.MetaHandler.StatementOrigin;
import de.benjaminaaron.ontoengine.model.dataset.DatasetProvider;
import de.benjaminaaron.ontoengine.model.graph.GraphManager;
import de.benjaminaaron.ontoengine.routing.ChangeListener;
import de.benjaminaaron.ontoengine.routing.websocket.WebSocketRouting;
import de.benjaminaaron.ontoengine.routing.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoengine.routing.websocket.messages.AddStatementResponse;
import de.benjaminaaron.ontoengine.suggestion.SuggestionEngine;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ModelController {

    private final Logger logger = LogManager.getLogger(ModelController.class);

    @Autowired
    private WebSocketRouting router;

    @Autowired
    private SuggestionEngine suggestionEngine;

    private final Dataset dataset;
    private final Model mainModel;
    private final Model metaModel;
    private final Model vocabularySourcesModel;

    // private final FusekiServer fusekiServer;

    @Value("classpath:meta.owl")
    private Path META_OWL;

    @Autowired
    private MetaHandler metaHandler;

    // this should be a @Component
    private final GraphManager graphManager;

    private final Set<ChangeListener> changeListeners = new HashSet<>();

    public ModelController(
            DatasetProvider datasetProvider,
            @Value("${jena.tdb.directory}") Path TBD_DIR,
            @Value("${jena.tdb.model.main.name}") String MAIN_MODEL_NAME,
            @Value("${jena.tdb.model.meta.name}") String META_MODEL_NAME,
            @Value("${jena.tdb.model.vocabulary-sources.name}") String VOCABULARY_SOURCES_MODEL_NAME,
            @Value("${uri.default.namespace}") String DEFAULT_URI_NAMESPACE
    ) {
        Utils.DEFAULT_URI_NAMESPACE = DEFAULT_URI_NAMESPACE;

        dataset = datasetProvider.getDataset();

        // Main Model
        if (!dataset.containsNamedModel(MAIN_MODEL_NAME)) {
            logger.info("Creating " + MAIN_MODEL_NAME + "-model in TDB location '" + TBD_DIR + "'");
        }
        // these are actually what is called "named graphs" elsewhere
        // support adding/querying over entire dataset vs. specific named graphs/models TODO
        mainModel = dataset.getNamedModel(MAIN_MODEL_NAME);
        mainModel.setNsPrefix("onto", DEFAULT_URI_NAMESPACE);
        // Meta Model
        if (!dataset.containsNamedModel(META_MODEL_NAME)) {
            logger.info("Creating " + META_MODEL_NAME + "-model in TDB location '" + TBD_DIR + "'");
        }
        metaModel = dataset.getNamedModel(META_MODEL_NAME);
        metaModel.setNsPrefix("meta", MetaHandler.META_NS + "#");
        // Vocabulary Sources Model
        if (!dataset.containsNamedModel(VOCABULARY_SOURCES_MODEL_NAME)) {
            logger.info("Creating " + VOCABULARY_SOURCES_MODEL_NAME + "-model in TDB location '" + TBD_DIR + "'");
        }
        vocabularySourcesModel = dataset.getNamedModel(VOCABULARY_SOURCES_MODEL_NAME);

        graphManager = new GraphManager(mainModel);
        printStatements();

        /*
        https://jena.apache.org/documentation/fuseki2/fuseki-embedded.html
        https://jena.apache.org/documentation/txn/txn.html
        fusekiServer = FusekiServer.create()
            .add("/", dataset)
            .build();
        fusekiServer.start();
        */
    }

    @PostConstruct
    private void init() {
        metaHandler.init(mainModel, metaModel, META_OWL);
    }

    @PreDestroy
    private void close() {
        mainModel.close();
        metaModel.close();
        vocabularySourcesModel.close();
        // fusekiServer.stop();
    }

    public AddStatementResponse addStatement(AddStatementMessage statementMsg, boolean doLogging) {
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
            if (doLogging) {
                broadcastToChangeListeners("Statement not added: it exists already");
            }
            return response;
        }
        addStatement(statement, StatementOrigin.ADD, "client", response, doLogging);
        router.sendNewTripleEvent(statement);
        // suggestionEngine.runNewStatementJob(statement);
        return response;
    }

    public void addStatement(Statement statement, StatementOrigin origin, String info, AddStatementResponse response, boolean doLogging) {
        metaHandler.storeNewTripleEvent(statement, origin, info, response);
        mainModel.add(statement);
        if (doLogging) {
            String text = "Statement added: " + statement.getSubject() + ", " + statement.getPredicate() + ", " + statement.getObject();
            logger.info(text);
            broadcastToChangeListeners(text);
        }
        graphManager.importStatement(statement);
    }

    public void replaceUris(Set<String> from, String to) {
        // simpler approach?
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
                // extract the to-resources once for all before the loop?
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
        graphManager.replaceUris(from, to);
        metaHandler.storeUrisRenameEvent(from, to, "client");
        router.sendMessage(replaceCount + " URIs in " + insertionList.size() + " statements replaced");
    }

    public Model getMainModel() {
        return mainModel;
    }

    public Model getMetaModel() {
        return metaModel;
    }

    public Model getVocabularySourcesModel() {
        return vocabularySourcesModel;
    }

    public MetaHandler getMetaHandler() {
        return metaHandler;
    }

    public GraphManager getGraphManager() {
        return graphManager;
    }

    public void printStatements() {
        mainModel.listStatements().toList().forEach(System.out::println);
    }

    public void dev(String optionalQuery) {
        String query =
                "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#> " +
                "PREFIX list: <http://jena.hpl.hp.com/ARQ/list#> " +
                "SELECT * WHERE { " +
                "   BIND(\"A pred1 B pred2 C --> A pred3 C\" AS ?str)." +
                "   ?parts apf:strSplit(?str \"-->\") . " +
                //"   ?x :list ?ls . " +
                //"   ?ls list:index (?pos ?parts). " +
                "}";
        if (Objects.nonNull(optionalQuery)) {
            query = optionalQuery;
        }
        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, mainModel)) {
            ResultSet resultSet = queryExecution.execSelect();
            ResultSetFormatter.out(resultSet);
        }
    }

    public void broadcastToChangeListeners(String msg) {
        changeListeners.forEach(listener -> listener.broadcast(msg));
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    public String generateStatistics() {
        // add more interesting stats? TODO
        return "Triples in main model: " + countAllTriples(mainModel)
            +", triples in meta model: " + countAllTriples(metaModel);
    }

    private int countAllTriples(Model model) {
        String query = "SELECT (COUNT(*) AS ?triples) WHERE { ?s ?p ?o . }";
        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
            ResultSet resultSet = queryExecution.execSelect();
            return resultSet.next().get("triples").asLiteral().getInt();
        }
    }

    public void clearAll() {
        mainModel.removeAll();
        metaModel.removeAll();
        graphManager.resetGraph();
        // pending suggestions etc.? TODO
        String text = "Everything has been cleared: the persisted main and meta models and the in-memory graph";
        logger.info(text);
        broadcastToChangeListeners(text);
    }

    public String runSelectQuery(String wherePart) {
        String query = "PREFIX : <http://onto.de/default#> SELECT * WHERE { " + wherePart + " }";
        try (QueryExecution queryExecution = QueryExecutionFactory.create(query, mainModel)) {
            ResultSet resultSet = queryExecution.execSelect();
            return ResultSetFormatter.asText(resultSet);
        }
    }
}
