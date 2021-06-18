package de.benjaminaaron.ontoserver.model.suggestion.job;

import de.benjaminaaron.ontoserver.model.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.model.suggestion.job.task.JobTask;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.ArrayList;
import java.util.List;

public class CompareOneToAllStatementsJob extends Job {

    private final Statement statement;

    public CompareOneToAllStatementsJob(Model model, Statement statement) {
        super(model);
        this.statement = statement;
    }

    @Override
    public List<Suggestion> execute() {
        List<Suggestion> suggestions = new ArrayList<>();
        StmtIterator iterator = model.listStatements();
        while(iterator.hasNext()) {
            Statement stmt = iterator.nextStatement();
            if (stmt.equals(statement)) {
                continue;
            }
            for (JobTask task : tasks) {
                // task.evaluateStatement(stmt);
            }
        }
        for (JobTask task : tasks) {
            suggestions.addAll(task.collectSuggestions());
        }
        return suggestions;
    }
}
