package de.benjaminaaron.ontoserver.model.graph;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.nio.graphml.GraphMLExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Graph {

    private final DefaultDirectedGraph<Node, Edge> graph;
    private final Map<RDFNode, Node> nodesMap = new HashMap<>();

    public Graph(Model model) {
        graph = new DefaultDirectedGraph<>(Edge.class);
        model.listStatements().toList().forEach(this::importStatement);
    }

    public void importStatement(Statement statement) {
        RDFNode subject = statement.getSubject();
        Property predicate = statement.getPredicate();
        RDFNode object = statement.getObject();
        Edge edge = graph.addEdge(getOrAdd(subject), getOrAdd(object));
        edge.setProperty(predicate);
    }

    private Node getOrAdd(RDFNode rdfNode) {
        if (nodesMap.containsKey(rdfNode)) {
            return nodesMap.get(rdfNode);
        }
        Node node = new Node(rdfNode);
        nodesMap.put(rdfNode, node);
        graph.addVertex(node);
        return node;
    }

    public void exportGraphml() {
        GraphMLExporter<Node, Edge> exporter = new GraphMLExporter<>();
        // use Edit > "Properties Mapper..." to map VertexLabel and EdgeLabel to LabelText
        exporter.setExportVertexLabels(true);
        exporter.setExportEdgeLabels(true);
        try {
            FileWriter fileWriter = new FileWriter("out.graphml");
            exporter.exportGraph(graph, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
