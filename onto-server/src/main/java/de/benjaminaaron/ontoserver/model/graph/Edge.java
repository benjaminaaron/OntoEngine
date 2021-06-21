package de.benjaminaaron.ontoserver.model.graph;

import org.apache.jena.rdf.model.Property;

public class Edge {

    Property property;

    public void setProperty(Property property) {
        this.property = property;
    }

    public String getLocalNameFromUri() {
        return property.getLocalName();
    }

    @Override
    public String toString() {
        return property.toString();
    }
}
