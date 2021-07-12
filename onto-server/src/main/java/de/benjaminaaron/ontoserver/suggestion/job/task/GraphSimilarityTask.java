package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.graph.Edge;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobGraphTask;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphSimilarityTask extends JobGraphTask {

    private final Logger logger = LogManager.getLogger(GraphSimilarityTask.class);

    @Override
    public List<Suggestion> execute() {
        Graph<RDFNode, Edge> graph = graphManager.getGraph();
        List<RDFNode> vertices = new ArrayList<>(graph.vertexSet());
        int n = vertices.size();
        n = n * (n - 1) / 2;
        logger.info("GraphSimilarityTask: comparing " + n + " pairs, formed from " + vertices.size() + " vertices");
        for (int a = 0; a < vertices.size() - 1; a++) {
            RDFNode vertexA = vertices.get(a);
            Set<Edge> incomingA = graph.incomingEdgesOf(vertexA);
            Set<Edge> outgoingA = graph.outgoingEdgesOf(vertexA);
            Set<RDFNode> firstDegreeVerticesFromIncomingA = incomingA.stream().map(graph::getEdgeSource).collect(Collectors.toSet());
            Set<RDFNode> firstDegreeVerticesFromOutgoingA = outgoingA.stream().map(graph::getEdgeTarget).collect(Collectors.toSet());
            
            int score = 0;
            for (int b = a + 1; b < vertices.size(); b++) {
                RDFNode vertexB = vertices.get(b);
                Set<Edge> incomingB = graph.incomingEdgesOf(vertexB);
                Set<Edge> outgoingB = graph.outgoingEdgesOf(vertexB);
                Set<RDFNode> firstDegreeVerticesFromIncomingB = incomingB.stream().map(graph::getEdgeSource).collect(Collectors.toSet());
                Set<RDFNode> firstDegreeVerticesFromOutgoingB = outgoingB.stream().map(graph::getEdgeTarget).collect(Collectors.toSet());

                Sets.SetView<Edge> sharedEdgesIncoming = Sets.intersection(incomingA, incomingB);
                Sets.SetView<Edge> sharedEdgesOutgoing = Sets.intersection(outgoingA, outgoingB);
                Sets.SetView<RDFNode> sharedFirstDegreeVerticesFromIncoming = Sets.intersection(firstDegreeVerticesFromIncomingA, firstDegreeVerticesFromIncomingB);
                Sets.SetView<RDFNode> sharedFirstDegreeVerticesFromOutgoing = Sets.intersection(firstDegreeVerticesFromOutgoingA, firstDegreeVerticesFromOutgoingB);

                Set<Edge> exactSharedEdgesIncoming = incomingA.stream().filter(edge -> containsWithSameSource(edge, incomingB, graph)).collect(Collectors.toSet());
                Set<Edge> exactSharedEdgesOutgoing = outgoingA.stream().filter(edge -> containsWithSameTarget(edge, outgoingB, graph)).collect(Collectors.toSet());

                score = sharedEdgesIncoming.size() + sharedEdgesOutgoing.size() +
                        sharedFirstDegreeVerticesFromIncoming.size() + sharedFirstDegreeVerticesFromOutgoing.size() +
                        exactSharedEdgesIncoming.size() + exactSharedEdgesOutgoing.size();

                System.out.println(vertexA + " <--> " + vertexB + " __ " + score);
                System.out.println("sharedEdgesIncoming: " + sharedEdgesIncoming);
                System.out.println("exactSharedEdgesIncoming: " + exactSharedEdgesIncoming);
                System.out.println("sharedEdgesOutgoing: " + sharedEdgesOutgoing);
                System.out.println("exactSharedEdgesOutgoing: " + exactSharedEdgesOutgoing);
                System.out.println("sharedFirstDegreeVerticesFromIncoming: " + sharedFirstDegreeVerticesFromIncoming);
                System.out.println("sharedFirstDegreeVerticesFromOutgoing: " + sharedFirstDegreeVerticesFromOutgoing);
                System.out.println();
            }
        }
        // suggestions.add(new Suggestion(null));
        // compare n pairs of edges too?
        return suggestions;
    }

    private boolean containsWithSameSource(Edge edgeToCheck, Set<Edge> set, Graph<RDFNode, Edge> graph) {
        if (!set.contains(edgeToCheck)) {
            return false;
        }
        RDFNode source = graph.getEdgeSource(edgeToCheck);
        return set.stream().anyMatch(edge -> source.equals(graph.getEdgeSource(edge)));
    }

    private boolean containsWithSameTarget(Edge edgeToCheck, Set<Edge> set, Graph<RDFNode, Edge> graph) {
        if (!set.contains(edgeToCheck)) {
            return false;
        }
        RDFNode target = graph.getEdgeTarget(edgeToCheck);
        return set.stream().anyMatch(edge -> target.equals(graph.getEdgeTarget(edge)));
    }
}
