package de.benjaminaaron.ontoserver.jena;

import org.apache.jena.rdf.model.*;
import org.springframework.stereotype.Component;

@Component
public class JenaController {

    public JenaController() {
        Model model = ModelFactory.createDefaultModel();

        Resource sub = model.createResource("http://onto/Benjamin");
        Property pred = model.createProperty("http://onto/isA");
        RDFNode obj = model.createResource("http://onto/Human");

        Statement statement = ResourceFactory.createStatement(sub, pred, obj);
        model.add(statement);

        System.out.println(model.listStatements().toSet());
    }
}
