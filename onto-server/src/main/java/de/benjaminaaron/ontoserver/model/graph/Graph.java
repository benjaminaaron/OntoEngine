package de.benjaminaaron.ontoserver.model.graph;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    private final DirectedMultigraphWithSelfLoops<Node, Edge> graph;
    private final Map<RDFNode, Node> nodes = new HashMap<>();
    private final Map<Property, Edge> edges = new HashMap<>();
    private final Model mainModel;

    // integrate more tightly with the Jena Graph?
    // https://github.com/SmartDataAnalytics/SubgraphIsomorphismIndex/blob/master/jena-jgrapht-bindings/src/main/java/org/aksw/commons/jena/jgrapht/PseudoGraphJenaGraph.java
    public Graph(Model mainModel) {
        this.mainModel = mainModel;
        graph = new DirectedMultigraphWithSelfLoops<>(Edge.class);
        mainModel.listStatements().toList().forEach(this::importStatement);
    }

    public void importStatement(Statement statement) {
        RDFNode subject = statement.getSubject();
        Property predicate = statement.getPredicate();
        RDFNode object = statement.getObject();
        Edge edge = graph.addEdge(getOrAdd(subject), getOrAdd(object));
        edge.property = predicate;
        edges.put(predicate, edge);
    }

    private Node getOrAdd(RDFNode rdfNode) {
        if (nodes.containsKey(rdfNode)) {
            return nodes.get(rdfNode);
        }
        Node node = new Node(rdfNode);
        nodes.put(rdfNode, node);
        graph.addVertex(node);
        return node;
    }

    public void replaceUris(Set<String> from, String to) {
        // combine more logic with ModelController.replaceUris() ?
        List<RDFNode> nodesToBeChanged = nodes.keySet().stream().filter(rdfNode ->
                !rdfNode.isLiteral() && from.contains(rdfNode.asResource().getURI())).collect(Collectors.toList());
        if (!nodesToBeChanged.isEmpty()) {
            Node toNode = getOrAdd(mainModel.createResource(to));
            for (RDFNode fromRdfNode : nodesToBeChanged) {
                Node fromNode = nodes.get(fromRdfNode);
                for (Edge oldEdge : graph.outgoingEdgesOf(fromNode)) {
                    edges.remove(oldEdge.property);
                    Node target = graph.getEdgeTarget(oldEdge);
                    if (!graph.containsEdge(toNode, target)) {
                        Edge edge = graph.addEdge(toNode, target);
                        edge.property = oldEdge.property;
                        edges.put(edge.property, edge);
                    }
                }
                for (Edge oldEdge : graph.incomingEdgesOf(fromNode)) {
                    edges.remove(oldEdge.property);
                    Node source = graph.getEdgeSource(oldEdge);
                    if (!graph.containsEdge(source, toNode)) {
                        Edge edge = graph.addEdge(source, toNode);
                        edge.property = oldEdge.property;
                        edges.put(edge.property, edge);
                    }
                }
                graph.removeVertex(fromNode);
                nodes.remove(fromRdfNode);
            }
        }

        List<Property> edgesToBeChanged = edges.keySet().stream().filter(property ->
                from.contains(property.getURI())).collect(Collectors.toList());
        if (!edgesToBeChanged.isEmpty()) {
            Property toProperty = mainModel.createProperty(to);
            for (Property oldProperty : edgesToBeChanged) {
                Edge edge = edges.get(oldProperty);
                edge.property = toProperty;
                edges.remove(oldProperty);
                edges.put(toProperty, edge);
            }
        }
    }

    public void exportGraphml(File file, boolean fullUri) {
        GraphMLExporter<Node, Edge> exporter = new GraphMLExporter<>();

        exporter.setVertexAttributeProvider(vertex -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(fullUri ? vertex.toString() : vertex.getLocalNameFromUri()));
            return map;
        });

        exporter.setVertexLabelAttributeName("label");
        exporter.setExportVertexLabels(true);

        exporter.setEdgeAttributeProvider(edge -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(fullUri ? edge.toString() : edge.getLocalNameFromUri()));
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
