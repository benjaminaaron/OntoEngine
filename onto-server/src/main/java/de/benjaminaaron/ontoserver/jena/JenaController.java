package de.benjaminaaron.ontoserver.jena;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static de.benjaminaaron.ontoserver.jena.Utils.ensureIri;

@Component
public class JenaController {

    @Value("${jena.tdb.directory}")
    private String TBD_DIR;
    private Model model;

    @PostConstruct
    private void init() {
        Dataset dataset = TDBFactory.createDataset(TBD_DIR) ;
        model = dataset.getDefaultModel();
        System.out.println(model.listStatements().toSet());
    }

    @PreDestroy
    private void close() {
        model.close();
    }

    public void addStatement(String subject, String predicate, String object) {
        Resource sub = model.createResource(ensureIri(subject));
        Property pred = model.createProperty(ensureIri(predicate));
        RDFNode obj = model.createResource(ensureIri(object));
        Statement statement = ResourceFactory.createStatement(sub, pred, obj);
        model.add(statement);
    }
}
