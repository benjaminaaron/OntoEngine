package de.benjaminaaron.ontoserver.model.graph;

import de.benjaminaaron.ontoserver.model.Utils;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Graph {

    private final DirectedMultigraphWithSelfLoops<RDFNode, Edge> graph;
    private final Model mainModel;

    // integrate more tightly with the Jena Graph?
    // https://github.com/SmartDataAnalytics/SubgraphIsomorphismIndex/blob/master/jena-jgrapht-bindings/src/main/java/org/aksw/commons/jena/jgrapht/PseudoGraphJenaGraph.java
    public Graph(Model mainModel) {
        this.mainModel = mainModel;
        graph = new DirectedMultigraphWithSelfLoops<>(Edge.class);
        mainModel.listStatements().toList().forEach(this::importStatement);
    }

    public void importStatement(Statement statement) {
        graph.addVertex(statement.getSubject());
        graph.addVertex(statement.getObject());
        graph.addEdge(statement.getSubject(), statement.getObject(), new Edge(statement.getPredicate()));
    }

    public void replaceUris(Set<String> from, String to) {
        List<RDFNode> verticesToBeChanged = graph.vertexSet().stream().filter(RDFNode::isResource)
                .filter(vertex -> from.contains(vertex.asResource().getURI())).collect(Collectors.toList());
        if (!verticesToBeChanged.isEmpty()) {
            RDFNode toVertex = mainModel.createResource(to);
            graph.addVertex(toVertex);
            for (RDFNode fromVertex : verticesToBeChanged) {
                graph.outgoingEdgesOf(fromVertex).forEach(oldEdge ->
                        addEdgeIfAbsent(toVertex, graph.getEdgeTarget(oldEdge), oldEdge.property));
                graph.incomingEdgesOf(fromVertex).forEach(oldEdge ->
                        addEdgeIfAbsent(graph.getEdgeSource(oldEdge), toVertex, oldEdge.property));
                graph.removeVertex(fromVertex);
            }
        }

        List<Edge> edgesToBeChanged = graph.edgeSet().stream()
                .filter(edge -> from.contains(edge.property.getURI())).collect(Collectors.toList());
        if (!edgesToBeChanged.isEmpty()) {
            Property toProperty = mainModel.createProperty(to);
            edgesToBeChanged.forEach(edge -> edge.property = toProperty);
        }
    }

    public void addEdgeIfAbsent(RDFNode source, RDFNode target, Property property) {
        if (!graph.containsEdge(source, target)) {
            graph.addEdge(source, target, new Edge(property));
        }
    }

    public void exportGraphml(File file, boolean fullUri) {
        GraphMLExporter<RDFNode, Edge> exporter = new GraphMLExporter<>();

        exporter.setVertexAttributeProvider(vertex -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(Utils.getValueFromRdfNode(vertex, fullUri)));
            return map;
        });
        exporter.setVertexLabelAttributeName("label");
        exporter.setExportVertexLabels(true);

        exporter.setEdgeAttributeProvider(edge -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(
                    fullUri ? edge.property.getURI() : edge.property.getLocalName()));
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
