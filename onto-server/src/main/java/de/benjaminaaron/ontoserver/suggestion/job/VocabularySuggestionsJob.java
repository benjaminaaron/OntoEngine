package de.benjaminaaron.ontoserver.suggestion.job;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

import java.util.List;

public class VocabularySuggestionsJob extends RunnableJob {

    private final Statement statement;

    public VocabularySuggestionsJob(Model model, Statement statement) {
        super(model);
        this.statement = statement;
    }

    @Override
    public List<Suggestion> execute() {
        start();

        // TODO

        stop();
        return suggestions;
    }
}
