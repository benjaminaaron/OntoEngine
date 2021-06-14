package de.benjaminaaron.ontoserver.model.suggestion.runthrough;

import de.benjaminaaron.ontoserver.model.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.model.suggestion.runthrough.task.RunThroughTask;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.ArrayList;
import java.util.List;

public class CompareOneToAllStatementsRunThrough extends RunThrough {

    private final StmtIterator iterator;
    private final Statement statement;

    public CompareOneToAllStatementsRunThrough(StmtIterator iterator, Statement statement) {
        this.iterator = iterator;
        this.statement = statement;
    }

    @Override
    public List<Suggestion> execute() {
        List<Suggestion> suggestions = new ArrayList<>();
        while(iterator.hasNext()) {
            Statement stmt = iterator.nextStatement();
            if (stmt.equals(statement)) {
                continue;
            }
            for (RunThroughTask task : tasks) {
                task.evaluateStatement(stmt);
            }
        }
        for (RunThroughTask task : tasks) {
            suggestions.addAll(task.collectSuggestions());
        }
        return suggestions;
    }
}
