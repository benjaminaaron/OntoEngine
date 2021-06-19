package de.benjaminaaron.ontoserver.model;

import de.benjaminaaron.ontoserver.model.graph.Graph;
import de.benjaminaaron.ontoserver.model.suggestion.SuggestionEngine;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementResponse;
import lombok.SneakyThrows;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.benjaminaaron.ontoserver.model.Utils.*;

@Component
public class ModelController {

    private final Logger logger = LogManager.getLogger(ModelController.class);

    @Value("${jena.tdb.directory}")
    private Path TBD_DIR;
    @Value("${model.export.directory}")
    private Path EXPORT_DIRECTORY;
    @Value("${graphdb.get-url}")
    private String GRAPHDB_GET_URL;
    @Value("${graphdb.insert-url}")
    private String GRAPHDB_INSERT_URL;
    @Value("${graphdb.default-repository}")
    private String GRAPHDB_DEFAULT_REPOSITORY;
    @Value("${graphdb.rest-url}")
    private String GRAPHDB_REST_URL;
    @Value("classpath:graphdb_repo_template.json")
    private Path GRAPHDB_REPO_TEMPLATE;

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

    private void addStatement(Statement statement) {
        logger.info("Statement added: " + statement.getSubject() + ", " + statement.getPredicate() + ", " + statement.getObject());
        model.add(statement);
        // graph.importStatement(statement);
    }

    public Model getModel() {
        return model;
    }

    public void printStatements() {
        model.listStatements().toList().forEach(System.out::println);
    }

    public void exportRDF() {
        try(FileOutputStream fos = new FileOutputStream(getExportFile(EXPORT_DIRECTORY, "model", "rdf"))) {
            model.write(fos, "RDF/XML");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportGraphml(boolean fullUri) {
        graph.exportGraphml(getExportFile(EXPORT_DIRECTORY, "model", "graphml"), fullUri);
    }

    public void importFromGraphDB(String repository) {
        try (RDFConnection conn = RDFConnectionFactory.connect(GRAPHDB_GET_URL.replace("<repository>", repository))) {
            Txn.executeRead(conn, () -> {
                String queryStr = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 5";
                conn.querySelect(QueryFactory.create(queryStr), qs -> {
                    Resource subj = qs.getResource("s");
                    Property pred = model.createProperty(qs.get("p").toString());
                    RDFNode obj = qs.get("o");
                    addStatement(ResourceFactory.createStatement(subj, pred, obj));
                });
            });
        }
    }

    @SneakyThrows
    public void exportToGraphDB() {
        // TODO allow passing of "clear" and "repo-name" param
        URL url = new URL(GRAPHDB_INSERT_URL.replace("<repository>", GRAPHDB_DEFAULT_REPOSITORY));
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("DELETE");
        http.setRequestProperty("Accept", "application/json");
        if (http.getResponseCode() != 204) {
            System.out.println("Could not delete the GraphDB repository");
        }
        http.disconnect();

        url = new URL(GRAPHDB_REST_URL);
        http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Accept", "application/json");
        String jsonStr = Files.readString(GRAPHDB_REPO_TEMPLATE, StandardCharsets.UTF_8)
                .replace("<id>", GRAPHDB_DEFAULT_REPOSITORY);
        http.getOutputStream().write(jsonStr.getBytes(StandardCharsets.UTF_8));
        if (http.getResponseCode() != 201) {
            System.out.println("Could not create GraphDB repository");
        }
        http.disconnect();

        try (RDFConnection conn = RDFConnectionFactory.connect(GRAPHDB_INSERT_URL.replace("<repository>", GRAPHDB_DEFAULT_REPOSITORY))) {
            Txn.executeWrite(conn, () -> {
                StmtIterator iterator = model.listStatements();
                while (iterator.hasNext()) {
                    Statement statement = iterator.nextStatement();
                    String sUri = statement.getSubject().getURI();
                    String pUri = statement.getPredicate().getURI();
                    String objStr = rdfNodeToGraphDatabaseEntryString(statement.getObject());
                    conn.update("INSERT DATA { <" + sUri + "> <" + pUri + "> " + objStr + " }");
                }
            });
        }
    }
}
