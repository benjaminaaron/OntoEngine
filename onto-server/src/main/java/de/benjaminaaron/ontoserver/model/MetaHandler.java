package de.benjaminaaron.ontoserver.model;

import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementResponse;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class MetaHandler {

    private final Logger logger = LogManager.getLogger(MetaHandler.class);
    private final Model mainModel, metaModel;

    public MetaHandler(Model mainModel, Model metaModel) {
        this.mainModel = mainModel;
        this.metaModel = metaModel;
    }

    public void logAddStatement(Statement addedStmt, StatementOrigin origin, String details, AddStatementResponse response) {
        logger.info("Statement added: " + addedStmt.getSubject() + ", " + addedStmt.getPredicate() + ", " + addedStmt.getObject());

        Resource subject = addedStmt.getSubject();
        Property predicate = addedStmt.getPredicate();
        RDFNode object = addedStmt.getObject();
        boolean subjectIsNew = !mainModel.getGraph().contains(subject.asNode(), Node.ANY, Node.ANY);
        boolean predicateIsNew = !mainModel.getGraph().contains(Node.ANY, predicate.asNode(), Node.ANY);
        boolean objectIsNew = !mainModel.getGraph().contains(Node.ANY, Node.ANY, object.asNode());

        if (Objects.nonNull(response)) {
            response.setStatementAdded(true);
            response.setSubjectIsNew(subjectIsNew);
            response.setPredicateIsNew(predicateIsNew);
            response.setObjectIsNew(objectIsNew);
        }

        // Statement metaStmt = ResourceFactory.createStatement(null, null, null);
        // TODO
    }

    public enum StatementOrigin {
        ADD, IMPORT, INFERENCE
    }
}
