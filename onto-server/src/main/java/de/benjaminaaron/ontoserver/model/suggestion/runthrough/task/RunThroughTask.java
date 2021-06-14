package de.benjaminaaron.ontoserver.model.suggestion.runthrough.task;

import de.benjaminaaron.ontoserver.model.suggestion.Suggestion;
import org.apache.jena.rdf.model.Statement;

import java.util.Collections;
import java.util.List;

public abstract class RunThroughTask {

    public void evaluateStatement(Statement stmt) {}

    public List<Suggestion> collectSuggestions() {
        return Collections.emptyList();
    }
}
