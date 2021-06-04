package de.benjaminaaron.ontoserver.model.graph;

import org.apache.jena.rdf.model.Property;

import static de.benjaminaaron.ontoserver.model.Utils.pathFromUri;

public class Edge {

    Property property;

    public void setProperty(Property property) {
        this.property = property;
    }

    public String getPathFromUri() {
        return pathFromUri(toString());
    }

    @Override
    public String toString() {
        return property.toString();
    }
}
