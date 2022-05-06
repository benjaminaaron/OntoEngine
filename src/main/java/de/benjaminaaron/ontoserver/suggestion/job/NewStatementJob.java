package de.benjaminaaron.ontoserver.suggestion.job;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import org.apache.jena.rdf.model.Statement;

import java.util.List;

public class NewStatementJob extends RunnableJob {

    private final Statement statement;

    public NewStatementJob(Statement statement) {
        this.statement = statement;
    }

    @Override
    public List<Suggestion> execute() {
        start();
        tasks.forEach(task -> {
            task.setStatement(statement);
            suggestions.addAll(task.execute());
        });
        stop();
        return suggestions;
    }
}
