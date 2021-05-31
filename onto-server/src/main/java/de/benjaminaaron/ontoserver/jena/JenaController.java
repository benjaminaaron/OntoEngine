package de.benjaminaaron.ontoserver.jena;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class JenaController {

    @Value("${jena.tdb.directory}")
    private String tdbDir;
    private Model model;

    @PostConstruct
    private void init() {
        Dataset dataset = TDBFactory.createDataset(tdbDir) ;
        model = dataset.getDefaultModel();

        Resource sub = model.createResource("http://onto/Benjamin");
        Property pred = model.createProperty("http://onto/isA");
        RDFNode obj = model.createResource("http://onto/Human");

        Statement statement = ResourceFactory.createStatement(sub, pred, obj);
        model.add(statement);

        System.out.println(model.listStatements().toSet());
    }

    @PreDestroy
    private void close() {
        model.close();
    }
}
