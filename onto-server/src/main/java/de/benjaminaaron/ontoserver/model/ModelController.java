package de.benjaminaaron.ontoserver.model;

import de.benjaminaaron.ontoserver.model.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static de.benjaminaaron.ontoserver.model.Utils.ensureIri;

@Component
public class ModelController {

    @Value("${jena.tdb.directory}")
    private String TBD_DIR;
    private Model model;
    private Graph graph;

    @PostConstruct
    private void init() {
        Dataset dataset = TDBFactory.createDataset(TBD_DIR) ;
        model = dataset.getDefaultModel();
        graph = new Graph(model);
        printStatements();
    }

    @PreDestroy
    private void close() {
        model.close();
    }

    public boolean addStatement(String subject, String predicate, String object) {
        Resource sub = model.createResource(ensureIri(subject));
        Property pred = model.createProperty(ensureIri(predicate));
        RDFNode obj = model.createResource(ensureIri(object));
        Statement statement = ResourceFactory.createStatement(sub, pred, obj);
        if (model.contains(statement)) {
            return false;
        }
        model.add(statement);
        graph.importStatement(statement);
        return true;
    }

    public void printStatements() {
        model.listStatements().toList().forEach(System.out::println);
    }

    public void exportRDF() {
        try(FileOutputStream fos = new FileOutputStream(Path.of("model.rdf").toFile())) {
            model.write(fos, "RDF/XML");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportGraphml() {
        graph.exportGraphml();
    }

    public void importFromSparqlEndpoint() {
        // TODO
    }

    public void exportToGraphDB() {
        // TODO
    }

    public void backupTdbDirectory() {
        // TODO
    }

    public void clearTDB() {
        // TODO
    }
}
