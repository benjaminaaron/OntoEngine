package de.benjaminaaron.ontoengine.suggestion.job.task;

import de.benjaminaaron.ontoengine.suggestion.Suggestion;
import de.benjaminaaron.ontoengine.suggestion.job.task.base.JobModelTask;
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
        String queryStr = "PREFIX : <http://onto.de/default#> " +
                "SELECT * WHERE { " +
                "   ?queryName :hasPeriodicQuery ?query " +
                "}";
        try(QueryExecution queryExecution = QueryExecutionFactory.create(queryStr, metaModel)) {
            ResultSet resultSet = queryExecution.execSelect();
            while(resultSet.hasNext()) {
                QuerySolution row = resultSet.next();
                periodicQueries.put(row.getResource("queryName").getURI(), row.getLiteral("query").getString());
            }
        }

        // EXECUTE THEM
        periodicQueries.forEach((queryName, query) -> {
            try(QueryExecution queryExecution = QueryExecutionFactory.create(query, mainModel)) {
                StmtIterator iter = queryExecution.execConstruct().listStatements();
                while(iter.hasNext()) {
                    Statement statement = iter.nextStatement();
                    System.out.println(queryName + " --> " + statement);
                    // TODO suggestion
                }
            }
        });

        return suggestions;
    }
}
