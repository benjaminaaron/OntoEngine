package de.benjaminaaron.ontoserver.model.graph;

import org.apache.jena.rdf.model.Property;

public class Edge {

    Property property;

    public Edge(Property property) {
        this.property = property;
    }
}
