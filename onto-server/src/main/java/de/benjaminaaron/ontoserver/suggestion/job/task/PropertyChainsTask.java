package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobModelTask;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;

import java.util.Iterator;
import java.util.List;

public class PropertyChainsTask extends JobModelTask {

    @Override
    public List<Suggestion> execute() {
        // fetch the queries from metaModel TODO
        
        String query = "PREFIX : <http://onto.de/default#> " +
                "CONSTRUCT { " +
                "  ?person :livesIn ?location . " +
                "} WHERE { " +
                "    ?person :isA :Human . " +
                "    ?person :rents ?rentingObject . " +
                "    ?rentingObject :locatedIn ?location . " +
                "}";

        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, mainModel)) {
            Iterator<Triple> resultTriples = queryExecution.execConstructTriples();
            while (resultTriples.hasNext()) {
                Triple triple = resultTriples.next();
                System.out.println("--> " + triple.getSubject() + " " + triple.getPredicate() + " " + triple.getObject());
            }
        }

        return suggestions;
    }
}
