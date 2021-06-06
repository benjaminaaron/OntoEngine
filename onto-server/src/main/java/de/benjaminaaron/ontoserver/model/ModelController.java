package de.benjaminaaron.ontoserver.model;

import de.benjaminaaron.ontoserver.model.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
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

import static de.benjaminaaron.ontoserver.model.Utils.ensureUri;
import static de.benjaminaaron.ontoserver.model.Utils.getExportFile;

@Component
public class ModelController {

    private final Logger logger = LogManager.getLogger(ModelController.class);

    @Value("${jena.tdb.directory}")
    private Path TBD_DIR;
    @Value("${model.export.directory}")
    private Path EXPORT_DIRECTORY;
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

    public boolean addStatement(String subject, String predicate, String object) {
        Resource sub = model.createResource(ensureUri(subject));
        Property pred = model.createProperty(ensureUri(predicate));
        RDFNode obj = model.createResource(ensureUri(object));
        Statement statement = ResourceFactory.createStatement(sub, pred, obj);
        if (model.contains(statement)) {
            return false;
        }
        logger.info("Statement added: " + subject + ", " + predicate + ", " + object);
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

    public void importFromSparqlEndpoint() {}

    public void exportToGraphDB() {}

    public void backupTdbDirectory() {}

    public void clearTDB() {}
}
