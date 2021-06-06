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

import static de.benjaminaaron.ontoserver.model.Utils.getExportFile;

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

    public boolean addStatement(String subjectUri, String predicateUri, String objectUri) {
        Resource sub = model.createResource(subjectUri);
        Property pred = model.createProperty(predicateUri);
        Resource obj = model.createResource(objectUri);
        Statement statement = ResourceFactory.createStatement(sub, pred, obj);
        if (model.contains(statement)) {
            return false;
        }
        logger.info("Statement added: " + subjectUri + ", " + predicateUri + ", " + objectUri);
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
                    RDFNode pred = qs.get("p");
                    RDFNode obj = qs.get("o");
                    // TODO case differentiation
                    addStatement(subj.getURI(), pred.asResource().getURI(), obj.asResource().getURI());
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
                    String oUri = statement.getObject().asResource().getURI();
                    // TODO handle resource vs. literal for object
                    conn.update("INSERT DATA { <" + sUri + "> <" + pUri + "> <" + oUri + "> }");
                }
            });
        }
    }
}
