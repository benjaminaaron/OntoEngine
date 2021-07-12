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
import java.util.HashSet;
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
                // if (!(vertexA.asResource().getLocalName().equals("Test1") && vertexB.asResource().getLocalName().equals("Test2"))) continue;

                Set<Edge> incomingB = graph.incomingEdgesOf(vertexB);
                Set<Edge> outgoingB = graph.outgoingEdgesOf(vertexB);

                Set<RDFNode> firstDegreeVerticesFromIncomingB = new HashSet<>();
                Set<RDFNode> firstDegreeVerticesFromOutgoingB = new HashSet<>();
                Set<Edge> sharedEdgesIncoming = new HashSet<>();
                Set<Edge> sharedEdgesOutgoing = new HashSet<>();
                Set<Edge> exactSharedEdgesIncoming = new HashSet<>();
                Set<Edge> exactSharedEdgesOutgoing = new HashSet<>();

                collectSets(true, incomingB, incomingA, firstDegreeVerticesFromIncomingB, sharedEdgesIncoming, exactSharedEdgesIncoming, graph);
                collectSets(false, outgoingB, outgoingA, firstDegreeVerticesFromOutgoingB, sharedEdgesOutgoing, exactSharedEdgesOutgoing, graph);

                Sets.SetView<RDFNode> sharedFirstDegreeVerticesFromIncoming = Sets.intersection(firstDegreeVerticesFromIncomingA, firstDegreeVerticesFromIncomingB);
                Sets.SetView<RDFNode> sharedFirstDegreeVerticesFromOutgoing = Sets.intersection(firstDegreeVerticesFromOutgoingA, firstDegreeVerticesFromOutgoingB);

                score = sharedEdgesIncoming.size() + sharedEdgesOutgoing.size() +
                        sharedFirstDegreeVerticesFromIncoming.size() + sharedFirstDegreeVerticesFromOutgoing.size() +
                        exactSharedEdgesIncoming.size() + exactSharedEdgesOutgoing.size();

                System.out.println(vertexA + " <--> " + vertexB + " __ " + score);
                System.out.println("sharedEdgesIncoming: " + sharedEdgesIncoming);
                System.out.println("exactSharedEdgesIncoming: " + exactSharedEdgesIncoming);
                System.out.println("sharedEdgesOutgoing: " + sharedEdgesOutgoing);
                System.out.println("exactSharedEdgesOutgoing: " + exactSharedEdgesOutgoing);
                System.out.println("firstDegreeVerticesFromIncomingA: " + firstDegreeVerticesFromIncomingA);
                System.out.println("firstDegreeVerticesFromOutgoingA: " + firstDegreeVerticesFromOutgoingA);
                System.out.println("firstDegreeVerticesFromIncomingB: " + firstDegreeVerticesFromIncomingB);
                System.out.println("firstDegreeVerticesFromOutgoingB: " + firstDegreeVerticesFromOutgoingB);
                System.out.println("sharedFirstDegreeVerticesFromIncoming: " + sharedFirstDegreeVerticesFromIncoming);
                System.out.println("sharedFirstDegreeVerticesFromIncoming: " + sharedFirstDegreeVerticesFromIncoming);
                System.out.println();
            }
        }
        // suggestions.add(new Suggestion(null));
        // compare n pairs of edges too?
        return suggestions;
    }

    private RDFNode getSourceOrTarget(boolean isIncoming, Edge edge, Graph<RDFNode, Edge> graph) {
        if (isIncoming) {
            return graph.getEdgeSource(edge);
        }
        return graph.getEdgeTarget(edge);
    }

    private void collectSets(boolean isIncoming, Set<Edge> edgesB, Set<Edge> edgesA, Set<RDFNode> firstDegreeVerticesB, Set<Edge> sharedEdges, Set<Edge> exactSharedEdges, Graph<RDFNode, Edge> graph) {
        edgesB.forEach(edge -> {
            RDFNode sourceOrTarget = getSourceOrTarget(isIncoming, edge, graph);
            firstDegreeVerticesB.add(sourceOrTarget);
            if (!edgesA.contains(edge)) {
                return;
            }
            // can't rely on add() to not add it if its already there, but overriding the hashCode() method makes it worse
            if (sharedEdges.stream().noneMatch(e -> e.equals(edge))) {
                sharedEdges.add(edge);
            }
            if (edgesA.stream().anyMatch(e -> sourceOrTarget.equals(getSourceOrTarget(isIncoming, e, graph)))) {
                exactSharedEdges.add(edge);
            }
        });
    }
}
