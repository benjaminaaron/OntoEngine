package de.benjaminaaron.ontoserver.model.suggestion.runthrough;

import de.benjaminaaron.ontoserver.model.suggestion.Suggestion;
import org.apache.jena.rdf.model.Model;

import java.util.List;

public class LinearRunThrough extends RunThrough {

    public LinearRunThrough(Model model) {
        super(model);
    }

    @Override
    public List<Suggestion> execute() {
        System.out.println("size: " + model.listStatements().toList().size());
        return null;
    }
}
