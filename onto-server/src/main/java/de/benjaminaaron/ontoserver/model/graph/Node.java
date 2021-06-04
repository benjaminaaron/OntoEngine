package de.benjaminaaron.ontoserver.model.graph;

import org.apache.jena.rdf.model.RDFNode;

import static de.benjaminaaron.ontoserver.model.Utils.pathFromUri;

public class Node {

    RDFNode rdfNode;

    public Node(RDFNode rdfNode) {
        this.rdfNode = rdfNode;
    }

    public String getPathFromUri() {
        return pathFromUri(toString());
    }

    @Override
    public String toString() {
        return rdfNode.toString();
    }
}
