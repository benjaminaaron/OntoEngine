package de.benjaminaaron.ontoserver.suggestion.job;

import de.benjaminaaron.ontoserver.model.Utils.ResourceType;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.LocalVocabularyManager;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.List;

import static de.benjaminaaron.ontoserver.model.Utils.ResourceType.*;

public class VocabularySuggestionsJob extends RunnableJob {

    private final Statement statement;
    private final LocalVocabularyManager localVocabularyManager;

    public VocabularySuggestionsJob(Model model, Statement statement, LocalVocabularyManager localVocabularyManager) {
        super(model);
        this.statement = statement;
        this.localVocabularyManager = localVocabularyManager;
    }

    @Override
    public List<Suggestion> execute() {
        start();
        // this should be done in a JobTask, not here directly TODO
        check(statement.getSubject(), SUBJECT);
        check(statement.getPredicate(), PREDICATE);
        if (statement.getResource().isResource()) {
            check(statement.getObject().asResource(), OBJECT);
        }
        stop();
        return suggestions;
    }

    private void check(Resource resource, ResourceType resourceType) {
        localVocabularyManager.checkForMatches(resource, resourceType).forEach(msg -> suggestions.add(new Suggestion(msg)));
    }

}
