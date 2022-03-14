package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.suggestion.Query;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobModelTask;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.ArrayList;
import java.util.List;

import static de.benjaminaaron.ontoserver.suggestion.Query.QueryType.PERIODIC;

public class QueryExecutionTask extends JobModelTask {

    private final List<Query> queries;

    public QueryExecutionTask(List<Query> queries) {
        this.queries = queries;
        // for (Query query : queries) System.out.println(query.toString());
    }

    @Override
    public List<Suggestion> execute() {
        List<Suggestion> suggestions = new ArrayList<>();
        queries.stream().filter(q -> q.getType().equals(PERIODIC)).forEach(queryObj -> {
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

        });
        return suggestions;
    }
}
