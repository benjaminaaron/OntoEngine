package de.benjaminaaron.ontoserver.suggestion.job;

import de.benjaminaaron.ontoserver.model.Utils.ResourceType;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.VocabularyManager;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

import java.util.List;

public class VocabularySuggestionsJob extends RunnableJob {

    private final Statement statement;
    private final VocabularyManager vocabularyManager;

    public VocabularySuggestionsJob(Model model, Statement statement, VocabularyManager vocabularyManager) {
        super(model);
        this.statement = statement;
        this.vocabularyManager = vocabularyManager;
    }

    @Override
    public List<Suggestion> execute() {
        start();
        // this should be done in a JobTask, not here directly TODO
        addSuggestionIfNotNull(vocabularyManager.checkForMatchingResources(statement.getSubject(), ResourceType.SUBJECT));
        addSuggestionIfNotNull(vocabularyManager.checkForMatchingPredicates(statement.getPredicate()));
        if (statement.getResource().isResource()) {
            addSuggestionIfNotNull(vocabularyManager.checkForMatchingResources(statement.getObject().asResource(), ResourceType.OBJECT));
        }
        stop();
        return suggestions;
    }
}
