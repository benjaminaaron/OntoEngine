package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.graph.DirectedMultigraphWithSelfLoops;
import de.benjaminaaron.ontoserver.model.graph.Edge;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobGraphTask;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class GraphSimilarityTask extends JobGraphTask {

    private final Logger logger = LogManager.getLogger(GraphSimilarityTask.class);

    @Override
    public List<Suggestion> execute() {
        DirectedMultigraphWithSelfLoops<RDFNode, Edge> graph = graphManager.getGraph();
        List<RDFNode> vertices = new ArrayList<>(graph.vertexSet());
        int n = vertices.size();
        n = n * (n - 1) / 2;
        logger.info("GraphSimilarityTask: comparing " + n + " pairs, formed from " + vertices.size() + " vertices");
        for (int a = 0; a < vertices.size() - 1; a++) {
            RDFNode vertexA = vertices.get(a);
            for (int b = a + 1; b < vertices.size(); b++) {
                RDFNode vertexB = vertices.get(b);
                System.out.println(vertexA + " <--> " + vertexB);

                // TODO
            }
        }
        // suggestions.add(new Suggestion(null));
        // compare n pairs of edges too?
        return suggestions;
    }
}
