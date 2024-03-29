package de.benjaminaaron.ontoserver.model;

import de.benjaminaaron.ontoserver.model.MetaHandler.StatementOrigin;
import de.benjaminaaron.ontoserver.model.graph.GraphManager;
import de.benjaminaaron.ontoserver.routing.websocket.WebSocketRouting;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementResponse;
import de.benjaminaaron.ontoserver.suggestion.LocalVocabularyManager;
import de.benjaminaaron.ontoserver.suggestion.SuggestionEngine;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @Autowired
    private WebSocketRouting router;

    @Autowired
    private SuggestionEngine suggestionEngine;

    private final Model mainModel;
    private final Model metaModel;
    private final Model vocabularySourcesModel;
    private final GraphManager graphManager;
    private final MetaHandler metaHandler;
    private final LocalVocabularyManager localVocabularyManager;

    public ModelController(
            @Value("${jena.tdb.directory}") Path TBD_DIR,
            @Value("${jena.tdb.model.main.name}") String MAIN_MODEL_NAME,
            @Value("${jena.tdb.model.meta.name}") String META_MODEL_NAME,
            @Value("${jena.tdb.model.vocabulary-sources.name}") String VOCABULARY_SOURCES_MODEL_NAME,
            @Value("${uri.default.namespace}") String DEFAULT_URI_NAMESPACE,
            @Value("${uri.default.separator}") String DEFAULT_URI_SEPARATOR,
            @Value("classpath:meta.owl") Path META_OWL
    ) {
        Utils.DEFAULT_URI_NAMESPACE = DEFAULT_URI_NAMESPACE;
        Utils.DEFAULT_URI_SEPARATOR = DEFAULT_URI_SEPARATOR;

        Dataset dataset = TDBFactory.createDataset(TBD_DIR.toString());

        // Main Model
        if (!dataset.containsNamedModel(MAIN_MODEL_NAME)) {
            logger.info("Creating " + MAIN_MODEL_NAME + "-model in TDB location '" + TBD_DIR + "'");
        }
        mainModel = dataset.getNamedModel(MAIN_MODEL_NAME);
        // Meta Model
        if (!dataset.containsNamedModel(META_MODEL_NAME)) {
            logger.info("Creating " + META_MODEL_NAME + "-model in TDB location '" + TBD_DIR + "'");
        }
        metaModel = dataset.getNamedModel(META_MODEL_NAME);
        metaModel.setNsPrefix("meta", MetaHandler.META_NS + "#");
        metaHandler = new MetaHandler(mainModel, metaModel, META_OWL);
        // Vocabulary Sources Model
        if (!dataset.containsNamedModel(VOCABULARY_SOURCES_MODEL_NAME)) {
            logger.info("Creating " + VOCABULARY_SOURCES_MODEL_NAME + "-model in TDB location '" + TBD_DIR + "'");
        }
        vocabularySourcesModel = dataset.getNamedModel(VOCABULARY_SOURCES_MODEL_NAME);
        localVocabularyManager = new LocalVocabularyManager(vocabularySourcesModel);

        graphManager = new GraphManager(mainModel);
        printStatements();
    }

    @PreDestroy
    private void close() {
        mainModel.close();
        metaModel.close();
        vocabularySourcesModel.close();
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
        router.sendNewTripleEvent(statement);
        // suggestionEngine.runNewStatementJob(statement, localVocabularyManager);
        return response;
    }

    public void addStatement(Statement statement, StatementOrigin origin, String info, AddStatementResponse response) {
        metaHandler.storeNewTripleEvent(statement, origin, info, response);
        mainModel.add(statement);
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

    public MetaHandler getMetaHandler() {
        return metaHandler;
    }

    public GraphManager getGraph() {
        return graphManager;
    }

    public void printStatements() {
        mainModel.listStatements().toList().forEach(System.out::println);
    }
}
