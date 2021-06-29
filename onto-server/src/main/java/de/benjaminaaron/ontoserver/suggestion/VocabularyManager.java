package de.benjaminaaron.ontoserver.suggestion;

import org.apache.jena.rdf.model.Model;

import java.util.Set;

public class VocabularyManager {

    private final Model model;
    private static final Set<String> sourceUris = Set.of(
            "https://www.w3.org/2006/vcard/ns",
            "https://www.w3.org/2002/07/owl"
    );

    public VocabularyManager(Model model) {
        this.model = model;
        sourceUris.forEach(model::read);
        // model.write(System.out);
    }
}
