package de.benjaminaaron.ontoengine.domain.suggestion;

import de.benjaminaaron.ontoengine.domain.Utils.ResourceType;
import de.benjaminaaron.ontoengine.adapter.primary.messages.suggestion.VocabularySuggestionMessage;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.*;

import static de.benjaminaaron.ontoengine.domain.Utils.ResourceType.PREDICATE;

public class LocalVocabularyManager {

    private final Model model;
    private static final Set<String> sourceUris = Set.of(
            "https://www.w3.org/2006/vcard/ns"
            // "http://www.w3.org/1999/02/22-rdf-syntax-ns",
            // "http://www.w3.org/2000/01/rdf-schema",
            // "https://www.w3.org/2002/07/owl"
    );

    public LocalVocabularyManager(Model model) {
        this.model = model;
        // this fails without internet
        sourceUris.forEach(model::read);
        // model.write(System.out);
    }

    public List<VocabularySuggestionMessage> checkForMatches(Statement statement, ResourceType resourceType) {
        Resource source = resourceType.fromStatement(statement);
        List<VocabularySuggestionMessage> list = new ArrayList<>();

        // workaround to demonstrate inferable subClassOf-suggestion, TODO integrate properly
        if (PREDICATE == resourceType &&
                source.getLocalName().equalsIgnoreCase("subPropertyOf")
                && !source.getNameSpace().equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#")
        ) {
            VocabularySuggestionMessage message = new VocabularySuggestionMessage();
            message.setTaskName("LocalVocabularyMatchingTask");
            message.setResourceType(resourceType);
            message.setCurrentUri(source.getURI());
            String targetUri = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";
            message.setSuggestedUri(targetUri);
            message.setInfo("Use rdfs:subPropertyOf to make the connection inferable");
            String subUri = statement.getSubject().getURI();
            String objUri = statement.getObject().asResource().getURI(); // what if its a literal? TODO
            String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
            String owlObjProp = "http://www.w3.org/2002/07/owl#ObjectProperty";
            message.setAchievingCommand(
                    "REPLACE " + source.getURI() + " WITH " + targetUri + ", "
                    + "ADD " + subUri + " " + rdfType + " " + owlObjProp + ", " // child property
                    + "ADD " + objUri + " " + rdfType + " " + owlObjProp + ", " // parent property
            );
            list.add(message);
            return list;
        }

        runQuery(source.getLocalName(), resourceType).forEach((target, attributes) -> {
            VocabularySuggestionMessage message = new VocabularySuggestionMessage();
            message.setTaskName("LocalVocabularyMatchingTask");
            message.setResourceType(resourceType);
            message.setCurrentUri(source.getURI());
            message.setSuggestedUri(target.getURI());
            StringJoiner info = new StringJoiner(" | ");
            info.add(resourceType + ": the word '" + source.getLocalName()
                    + "' was found in an existing vocabulary, consider using " + target.getURI() + " instead");
            attributes.forEach((p, o) -> {
                String pShort = model.shortForm(p.getURI());
                String oShort = o.isResource() ? model.shortForm(o.asResource().getURI()) : o.toString();
                if ("rdf:type".equals(pShort)) {
                    info.add("RDF Type: " + oShort);
                }
                if ("rdfs:comment".equals(pShort)) {
                    info.add("Comment: " + oShort);
                }
                if ("rdfs:subClassOf".equals(pShort)) {
                    info.add("Subclass of: " + oShort);
                }
            });
            message.setInfo(info.toString());
            message.setAchievingCommand("REPLACE " + source.getURI() + " WITH " + target.getURI());
            list.add(message);
        });
        return list;
    }

    private Map<Resource, Map<Resource, RDFNode>> runQuery(String localName, ResourceType type) {
        String rdfTypeStr = "(owl:Class)";
        if (PREDICATE == type) {
            rdfTypeStr = "(owl:DatatypeProperty) (owl:ObjectProperty)";
        }
        String query =
                "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?s ?p ?o WHERE { " +
                "    VALUES (?sType) { " + rdfTypeStr + " } " +
                "    ?s rdf:type ?sType . " +
                "    FILTER(REGEX(LCASE(STR(?s)), '" + localName.toLowerCase() + "', 'i')) " +
                "    FILTER(isBlank(?s) = false) . " +
                "    ?s ?p ?o . " +
                "}" ;
        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
            ResultSet resultSet = queryExecution.execSelect();
            //ResultSetFormatter.out(resultSet);
            Map<Resource, Map<Resource, RDFNode>> map = new HashMap<>();
            while (resultSet.hasNext()) {
                QuerySolution querySolution = resultSet.next();
                Resource s = querySolution.getResource("s");
                map.putIfAbsent(s, new HashMap<>());
                map.get(s).put(querySolution.getResource("p"), querySolution.get("o"));
            }
            return map;
        }
    }
}
