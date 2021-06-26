package de.benjaminaaron.ontoserver.model.graph;

import org.apache.jena.rdf.model.Property;

public class Edge {

    Property property;

    public String getLocalNameFromUri() {
        return property.getLocalName();
    }

    @Override
    public String toString() {
        return property == null ? "no-property" : property.toString();
    }
}
