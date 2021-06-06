package de.benjaminaaron.ontoserver.model.graph;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Graph {

    private final DefaultDirectedGraph<Node, Edge> graph;
    private final Map<RDFNode, Node> nodesMap = new HashMap<>();

    // integrate more tightly with the Jena Graph?
    // https://github.com/SmartDataAnalytics/SubgraphIsomorphismIndex/blob/master/jena-jgrapht-bindings/src/main/java/org/aksw/commons/jena/jgrapht/PseudoGraphJenaGraph.java
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

    public void exportGraphml(File file, boolean fullUri) {
        GraphMLExporter<Node, Edge> exporter = new GraphMLExporter<>();

        exporter.setVertexAttributeProvider(vertex -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(fullUri ? vertex.toString() : vertex.getPathFromUri()));
            return map;
        });

        exporter.setVertexLabelAttributeName("label");
        exporter.setExportVertexLabels(true);

        exporter.setEdgeAttributeProvider(edge -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(fullUri ? edge.toString() : edge.getPathFromUri()));
            return map;
        });
        exporter.setEdgeLabelAttributeName("label");
        exporter.setExportEdgeLabels(true);

        // use Edit > "Properties Mapper..." to map "label" to "Label Text" for nodes and edges
        try {
            FileWriter fileWriter = new FileWriter(file);
            exporter.exportGraph(graph, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
