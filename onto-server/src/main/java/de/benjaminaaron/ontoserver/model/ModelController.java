package de.benjaminaaron.ontoserver.model;

import de.benjaminaaron.ontoserver.model.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private Model model;
    private Graph graph;

    @PostConstruct
    private void init() {
        Dataset dataset = TDBFactory.createDataset(TBD_DIR.toString()) ;
        model = dataset.getDefaultModel();
        graph = new Graph(model);
        printStatements();
    }

    @PreDestroy
    private void close() {
        model.close();
    }

    public boolean addStatement(String subject, String predicate, String object, boolean objectIsLiteral) {
        Resource sub = model.createResource(ensureUri(subject));
        Property pred = model.createProperty(ensureUri(predicate));
        RDFNode obj;
        if (objectIsLiteral) {
            obj = model.createTypedLiteral(detectLiteralType(object));
        } else {
            obj = model.createResource(ensureUri(object));
        }
        Statement statement = ResourceFactory.createStatement(sub, pred, obj);
        return addStatement(statement);
    }

    private boolean addStatement(Statement statement) {
        if (model.contains(statement)) {
            return false;
        }
        logger.info("Statement added: " + statement.getSubject() + ", " + statement.getPredicate() + ", " + statement.getObject());
        model.add(statement);
        graph.importStatement(statement);
        return true;
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

    public void exportToGraphDB() {
        try (RDFConnection conn = RDFConnectionFactory.connect(GRAPHDB_INSERT_URL.replace("<repository>", GRAPHDB_DEFAULT_REPOSITORY))) {
            Txn.executeWrite(conn, () -> {
                StmtIterator iterator = model.listStatements();
                while (iterator.hasNext()) {
                    Statement statement = iterator.nextStatement();
                    String sUri = statement.getSubject().getURI();
                    String pUri = statement.getPredicate().getURI();
                    String objStr;
                    if (statement.getObject().isLiteral()) {
                        objStr = "\"" + statement.getObject().asLiteral().toString() + "\"";
                    } else {
                        objStr = "<" + statement.getObject().asResource().getURI() + ">";
                    }
                    conn.update("INSERT DATA { <" + sUri + "> <" + pUri + "> " + objStr + " }");
                }
            });
        }
    }
}
