package de.benjaminaaron.ontoserver.model.graph;

import de.benjaminaaron.ontoserver.model.Utils;
import org.apache.jena.rdf.model.RDFNode;

public class Node {

    RDFNode rdfNode;

    public Node(RDFNode rdfNode) {
        this.rdfNode = rdfNode;
    }

    public boolean isLiteralNode() {
        return rdfNode.isLiteral();
    }

    public String getLocalNameFromUri() {
        if (rdfNode.isResource()) {
            return rdfNode.asResource().getLocalName();
        }
        if (rdfNode.isLiteral()) {
            return Utils.getValueFromLiteral(rdfNode.asLiteral());
        }
        return rdfNode.toString();
    }

    @Override
    public String toString() {
        return rdfNode.toString();
    }
}
