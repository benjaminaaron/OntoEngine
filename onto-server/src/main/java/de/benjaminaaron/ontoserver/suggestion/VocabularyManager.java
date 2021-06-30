package de.benjaminaaron.ontoserver.suggestion;

import de.benjaminaaron.ontoserver.model.Utils.ResourceType;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.VocabularySuggestionMessage;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.util.Set;

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

    public VocabularySuggestionMessage checkForMatchingResources(Resource resource, ResourceType resourceType) {
        VocabularySuggestionMessage message = new VocabularySuggestionMessage();
        message.setResourceType(resourceType);

        // TODO
        
        return null;
    }

    public VocabularySuggestionMessage checkForMatchingPredicates(Property predicate) {
        VocabularySuggestionMessage message = new VocabularySuggestionMessage();
        message.setResourceType(ResourceType.PREDICATE);

        // TODO

        return null;
    }
}
