package de.benjaminaaron.ontoserver.model;

import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementResponse;
import lombok.SneakyThrows;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

public class MetaHandler {

    private final Logger logger = LogManager.getLogger(MetaHandler.class);
    private OntModel metaDataModel;
    private final Model mainModel;
    private final OntClass NewTripleEvent, UrisRenameEvent, Triple;
    private final ObjectProperty hasTriple;
    private final DatatypeProperty hasInfo, hasOrigin, hasTimestamp, objectIsLiteral, objectIsNew, objectUri,
            predicateIsNew, predicateUri, renamedFrom, renamedTo, subjectIsNew, subjectUri;
    public static final String META_NS = "http://onto.de/meta.owl";

    @SneakyThrows
    public MetaHandler(Model mainModel, Model metaModel, Path meta_owl) {
        this.mainModel = mainModel;
        metaDataModel = ModelFactory.createOntologyModel(OntModelSpec.getDefaultSpec(ProfileRegistry.OWL_LANG), metaModel);
        OntModel metaOntology = ModelFactory.createOntologyModel();
        try (InputStream in = new FileInputStream(meta_owl.toFile())) {
            RDFParser.create().source(in).lang(RDFLanguages.RDFXML).parse(metaOntology);
        }
        // Classes
        NewTripleEvent = metaOntology.getOntClass(META_NS + "#NewTripleEvent");
        UrisRenameEvent = metaOntology.getOntClass(META_NS + "#UrisRenameEvent");
        Triple = metaOntology.getOntClass(META_NS + "#Triple");
        // Object properties
        hasTriple = metaOntology.getObjectProperty(META_NS + "#hasTriple");
        // Data properties
        hasInfo = metaOntology.getDatatypeProperty(META_NS + "#hasInfo");
        hasOrigin = metaOntology.getDatatypeProperty(META_NS + "#hasOrigin");
        hasTimestamp = metaOntology.getDatatypeProperty(META_NS + "#hasTimestamp");
        objectIsLiteral = metaOntology.getDatatypeProperty(META_NS + "#objectIsLiteral");
        objectIsNew = metaOntology.getDatatypeProperty(META_NS + "#objectIsNew");
        objectUri = metaOntology.getDatatypeProperty(META_NS + "#objectUri");
        predicateIsNew = metaOntology.getDatatypeProperty(META_NS + "#predicateIsNew");
        predicateUri = metaOntology.getDatatypeProperty(META_NS + "#predicateUri");
        renamedFrom = metaOntology.getDatatypeProperty(META_NS + "#renamedFrom");
        renamedTo = metaOntology.getDatatypeProperty(META_NS + "#renamedTo");
        subjectIsNew = metaOntology.getDatatypeProperty(META_NS + "#subjectIsNew");
        subjectUri = metaOntology.getDatatypeProperty(META_NS + "#subjectUri");
    }

    public void storeNewTripleEvent(Statement addedStmt, StatementOrigin origin, String info, AddStatementResponse response) {
        logger.info("Statement added: " + addedStmt.getSubject() + ", " + addedStmt.getPredicate() + ", " + addedStmt.getObject());

        Resource subject = addedStmt.getSubject();
        Property predicate = addedStmt.getPredicate();
        RDFNode object = addedStmt.getObject();
        boolean subjIsNew = !mainModel.getGraph().contains(subject.asNode(), Node.ANY, Node.ANY);
        boolean predIsNew = !mainModel.getGraph().contains(Node.ANY, predicate.asNode(), Node.ANY);
        boolean objIsNew = !mainModel.getGraph().contains(Node.ANY, Node.ANY, object.asNode());

        if (Objects.nonNull(response)) {
            response.setStatementAdded(true);
            response.setSubjectIsNew(subjIsNew);
            response.setPredicateIsNew(predIsNew);
            response.setObjectIsNew(objIsNew);
        }

        Individual triple = createIndividual(Triple);
        triple.addLiteral(subjectUri, subject.getURI());
        if (subjIsNew) {
            triple.addLiteral(subjectIsNew, true);
        }
        triple.addLiteral(predicateUri, predicate.getURI());
        if (predIsNew) {
            triple.addLiteral(predicateIsNew, true);
        }
        if (object.isLiteral()) {
            triple.addLiteral(objectIsLiteral, true);
        } else {
            triple.addLiteral(objectUri, object.asResource().getURI());
            if (objIsNew) {
                triple.addLiteral(objectIsNew, true);
            }
        }

        Individual newTripleEvent = createIndividual(NewTripleEvent);
        newTripleEvent.addLiteral(hasOrigin, origin.toString());
        newTripleEvent.addLiteral(hasInfo, info);
        newTripleEvent.addProperty(hasTriple, triple);
        newTripleEvent.addProperty(hasTimestamp, String.valueOf(Instant.now()), XSDDatatype.XSDdateTime);
    }

    private Individual createIndividual(OntClass ontClass) {
        int id = Iterators.size(metaDataModel.listSubjectsWithProperty(RDF.type, ontClass)) + 1;
        Individual individual = metaDataModel.createIndividual(ontClass.getURI() + id, ontClass);
        individual.addRDFType(OWL2.NamedIndividual);
        return individual;
    }

    public OntModel getMetaDataModel() {
        // Ontology ontology = metaDataModel.createOntology("http://onto.de/meta-data.owl");
        // ontology.addImport(metaDataModel.createResource("http://onto.de/meta.owl"));
        // ontology.removeImport(metaDataModel.getOntResource("http://onto.de/meta.owl"));
        return metaDataModel;
    }

    public enum StatementOrigin {
        ADD, IMPORT, INFERENCE
    }
}
