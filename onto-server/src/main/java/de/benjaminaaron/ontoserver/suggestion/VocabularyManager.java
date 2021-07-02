package de.benjaminaaron.ontoserver.suggestion;

import de.benjaminaaron.ontoserver.model.Utils;
import de.benjaminaaron.ontoserver.model.Utils.ResourceType;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.VocabularySuggestionMessage;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.*;

import static de.benjaminaaron.ontoserver.model.Utils.ResourceType.PREDICATE;

public class VocabularyManager {

    private final Model model;
    private static final Set<String> sourceUris = Set.of(
            "https://www.w3.org/2006/vcard/ns"
            //"https://www.w3.org/2002/07/owl"
    );

    public VocabularyManager(Model model) {
        this.model = model;
        sourceUris.forEach(model::read);
        // model.write(System.out);
    }

    public List<VocabularySuggestionMessage> checkForMatches(Resource source, ResourceType resourceType) {
        List<VocabularySuggestionMessage> list = new ArrayList<>();
        runQuery(source.getLocalName(), resourceType).forEach((target, attributes) -> {
            VocabularySuggestionMessage message = new VocabularySuggestionMessage();
            message.setResourceType(resourceType);
            message.setCurrentUri(source.getURI());
            message.setSuggestedUri(target.getURI());
            StringJoiner info = new StringJoiner(" | ");
            info.add(resourceType + ": the word '" + source.getLocalName()
                    + "' was found in an existing vocabulary, consider using " + target.getURI() + " instead");
            attributes.forEach((p, o) -> {
                String pShort = model.shortForm(p.getURI());
                String oShort = o.isResource() ? model.shortForm(o.asResource().getURI()) : Utils.getValueFromLiteral(o.asLiteral());
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
