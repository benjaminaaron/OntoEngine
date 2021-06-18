package de.benjaminaaron.ontoserver.model;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

// not in use but might be useful later on?
public class Entity {

    private final EntityType type;
    private final Object entity;

    // Subject
    public Entity(Resource resource) {
        this.entity = resource;
        this.type = EntityType.RESOURCE;
    }

    // Predicate
    public Entity(Node node) {
        this.entity = node;
        this.type = EntityType.PROPERTY;
    }

    // Object
    public Entity(RDFNode rdfNode) {
        if (rdfNode.isLiteral()) {
            this.entity = rdfNode.asLiteral();
            this.type = EntityType.LITERAL;
        } else if (rdfNode.isResource()) {
            this.entity = rdfNode.asResource();
            this.type = EntityType.RESOURCE;
        } else {
            throw new RuntimeException("Case not yet implemented");
        }
    }

    public String getUri() {
        switch (type) {
            case RESOURCE:
                return ((Resource) entity).getURI();
            case PROPERTY:
                return ((Node) entity).getURI();
            case LITERAL:
            default:
                return "";
        }
    }

    public String getWord() {
        switch (type) {
            case RESOURCE:
                return ((Resource) entity).getLocalName();
            case PROPERTY:
                return ((Node) entity).getLocalName();
            case LITERAL:
            default:
                return "";
        }
    }

    @Override
    public String toString() {
        return type + ": " + getUri();
    }

    private enum EntityType {
        RESOURCE, PROPERTY, LITERAL
    }
}
