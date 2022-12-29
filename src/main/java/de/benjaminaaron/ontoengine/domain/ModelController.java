package de.benjaminaaron.ontoengine.domain;

import static de.benjaminaaron.ontoengine.domain.MetaHandler.StatementOrigin.ADD;
import static de.benjaminaaron.ontoengine.domain.Utils.detectLiteralType;
import static de.benjaminaaron.ontoengine.domain.Utils.ensureUri;

import de.benjaminaaron.ontoengine.adapter.primary.ChangeListener;
import de.benjaminaaron.ontoengine.adapter.primary.WebSocketRouting;
import de.benjaminaaron.ontoengine.adapter.primary.messages.AddStatementMessage;
import de.benjaminaaron.ontoengine.adapter.primary.messages.AddStatementResponse;
import de.benjaminaaron.ontoengine.domain.MetaHandler.StatementOrigin;
import de.benjaminaaron.ontoengine.domain.dataset.DatasetProvider;
import de.benjaminaaron.ontoengine.domain.graph.GraphManager;
import de.benjaminaaron.ontoengine.domain.suggestion.SuggestionEngine;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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

    private final DatasetProvider datasetProvider;
    private Model mainModel;
    private Model metaModel;
    private GraphManager graphManager;
    private Model vocabularySourcesModel;

    // private final FusekiServer fusekiServer;

    @Value("classpath:meta.owl")
    private Path META_OWL;

    @Autowired
    private MetaHandler metaHandler;

    private final Set<ChangeListener> changeListeners = new HashSet<>();

    public ModelController(DatasetProvider datasetProvider) {
        this.datasetProvider = datasetProvider;
        mainModel = datasetProvider.getMainModel();
        metaModel = datasetProvider.getMetaModel();
        graphManager = datasetProvider.getGraphManager();
        vocabularySourcesModel = datasetProvider.getVocabularySourcesModel();

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
        addStatement(statement, ADD, "client", response, doLogging);
        router.sendNewTripleEvent(statement);
        // suggestionEngine.runNewStatementJob(statement);
        return response;
    }

    public boolean statementAlreadyPresent(Statement statement) {
        return mainModel.contains(statement);
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

    public String runSelectQueryUsingWherePart(String wherePart) {
        return "TODO restore functionality";
        // runSelectQuery("PREFIX : <http://onto.de/default#> SELECT * WHERE { " + wherePart + " }");
    }

    public JsonObject runCkgSelectQuery(String query) {
        // regex via ChatGPT and cleaned up via IntelliJ suggestions
        // would be nicer to extract it via query.getValuesVariables(), but didn't get that to work, was always null
        Matcher matcher = Pattern.compile("VALUES\\s+\\?\\S+\\s+\\{([^}]+)}").matcher(query);
        if (!matcher.find()) throw new RuntimeException("No VALUES clause found in the query: " + query);
        String valuesStr = matcher.group(1).trim().replace(":", "");
        Set<String> valuesInQuery =
            Arrays.stream(valuesStr.split(" ")).collect(Collectors.toSet());
        return sortIntoValuesFoundAndNotFound(query, valuesInQuery, new JsonObject());
    }

    public JsonObject sortIntoValuesFoundAndNotFound(String query, Set<String> valuesInQuery, JsonObject report) {
        try (QueryExecution queryExecution = QueryExecutionFactory.create(query, mainModel)) {
            ResultSet resultSet = queryExecution.execSelect();
            JsonObject valuesFound = new JsonObject();
            while(resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                String predLocalName = qs.getResource("p").getLocalName();
                if (valuesInQuery.contains(predLocalName)) {
                    valuesFound.put(predLocalName, qs.get("o").toString());
                    valuesInQuery.remove(predLocalName);
                }
            }
            JsonArray valuesNotFound = new JsonArray();
            valuesInQuery.forEach(valuesNotFound::add);
            report.put("valuesFound", valuesFound);
            report.put("valuesNotFound", valuesNotFound);
            return report;
        }
    }

    public JsonObject handleFormWorkflowTurtleFile(InputStream inputStream) {
        Model importModel = ModelFactory.createDefaultModel();
        importModel.read(inputStream, null, "TTL");
        String query = "PREFIX : <http://onto.de/default#> "
            + "SELECT * WHERE { "
            + "  ?s ?p ?o . "
            + "}";
        JsonObject fields = new JsonObject();
        try (QueryExecution queryExecution = QueryExecutionFactory.create(query, importModel)) {
            ResultSet resultSet = queryExecution.execSelect();
            Set<String> valuesInQuery = new HashSet<>();
            StringBuilder valuesQueryPart = new StringBuilder();
            valuesQueryPart.append("  VALUES ?p { ");
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                String subLocalName = qs.getResource("s").getLocalName();
                if (!subLocalName.startsWith("field")) continue;
                if (!fields.hasKey(subLocalName)) fields.put(subLocalName, new JsonObject());
                String predLocalName = qs.getResource("p").getLocalName();
                String obj = qs.get("o").isLiteral() ? qs.getLiteral("o").getString() : qs.getResource("o").getLocalName();
                fields.getObj(subLocalName).put(predLocalName, obj);
                if (predLocalName.equals("hasPredicate")) {
                    valuesInQuery.add(obj);
                    valuesQueryPart.append(":").append(obj).append(" ");
                }
            }
            query = "PREFIX : <http://onto.de/default#> "
                + "SELECT ?s ?p ?o WHERE { "
                + valuesQueryPart.append("} ")
                + "  ?s ?p ?o ."
                + "}";
            JsonObject report = new JsonObject();
            report.put("fields", fields);
            return sortIntoValuesFoundAndNotFound(query, valuesInQuery, report);
        }
    }

    public boolean addLocalNamesStatement(String sub, String pred, String obj) {
        Statement statement = mainModel.createStatement(
            mainModel.createResource(ensureUri(sub)),
            mainModel.createProperty(ensureUri(pred)),
            mainModel.createLiteral(obj)
        );
        if (statementAlreadyPresent(statement)) {
            return false;
        }
        addStatement(statement, ADD, "", null, false);
        return true;
    }
}
