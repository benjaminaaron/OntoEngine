package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobModelTask;

import java.util.ArrayList;
import java.util.List;

public class QueryExecutionTask extends JobModelTask {

    // private final List<Query> queries;

    public QueryExecutionTask() {
        // for (Query query : queries) System.out.println(query.toString());
    }

    @Override
    public List<Suggestion> execute() {
        List<Suggestion> suggestions = new ArrayList<>();
        /*queries.stream().filter(q -> q.getType().equals(PERIODIC)).forEach(queryObj -> {
            try(QueryExecution queryExecution = QueryExecutionFactory.create(queryObj.getQuery(), mainModel)) {
                List<Statement> statements = new ArrayList<>();
                StmtIterator iter = queryExecution.execConstruct().listStatements();
                while(iter.hasNext()) statements.add(iter.nextStatement());
                if (statements.size() > 0) {
                    System.out.println(queryObj.getQueryName() + " results:");
                    System.out.println(statements);
                    // TODO
                }
            }

        });*/
        return suggestions;
    }
}
