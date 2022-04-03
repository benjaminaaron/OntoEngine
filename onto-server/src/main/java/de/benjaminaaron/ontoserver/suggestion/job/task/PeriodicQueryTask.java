package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobModelTask;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeriodicQueryTask extends JobModelTask {

    private final Model metaModel;

    public PeriodicQueryTask(Model metaModel) {
        this.metaModel = metaModel;
    }

    @Override
    public List<Suggestion> execute() {
        List<Suggestion> suggestions = new ArrayList<>();

        // GET PERIODIC QUERIES
        Map<String, String> periodicQueries = new HashMap<>();
        String query = "PREFIX : <http://onto.de/default#> " +
                "SELECT * WHERE { " +
                "   ?queryName :hasPeriodicQuery ?query " +
                "}";
        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, metaModel)) {
            ResultSet resultSet = queryExecution.execSelect();
            while(resultSet.hasNext()) {
                QuerySolution row = resultSet.next();
                periodicQueries.put(row.getResource("queryName").getURI(), row.getLiteral("query").getString());
            }
        }
        System.out.println(periodicQueries);

        return suggestions;
    }
}
