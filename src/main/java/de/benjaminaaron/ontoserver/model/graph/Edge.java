package de.benjaminaaron.ontoserver.model.graph;

import org.apache.jena.rdf.model.Property;

public class Edge {

    public Property property;

    public Edge(Property property) {
        this.property = property;
    }

    @Override
    public boolean equals(Object other) {
        if (other.getClass() != Edge.class) {
            return false;
        }
        // graph.getEdgeSource(otherEdge).equals(graph.getEdgeSource(this)))
        // graph.getEdgeTarget(otherEdge).equals(graph.getEdgeTarget(this)))
        return property.getURI().equals(((Edge) other).property.getURI());
    }

    @Override
    public String toString() {
        return property.getURI();
    }
}
