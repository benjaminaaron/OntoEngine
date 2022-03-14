package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.suggestion.Query;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobTask;

import java.util.ArrayList;
import java.util.List;

public class QueryExecutionTask extends JobTask {

    private final List<Query> queries;

    public QueryExecutionTask(List<Query> queries) {
        this.queries = queries;
        // for (Query query : queries) System.out.println(query.toString());
    }

    @Override
    public List<Suggestion> execute() {
        List<Suggestion> suggestions = new ArrayList<>();
        // TODO
        return suggestions;
    }
}
