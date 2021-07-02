package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.Utils;
import de.benjaminaaron.ontoserver.suggestion.LocalVocabularyManager;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.List;

import static de.benjaminaaron.ontoserver.model.Utils.ResourceType.*;

public class LocalVocabularyMatchingTask extends JobTask {

    private final LocalVocabularyManager localVocabularyManager;
    private Statement statement;

    public LocalVocabularyMatchingTask(LocalVocabularyManager localVocabularyManager) {
        this.localVocabularyManager = localVocabularyManager;
    }

    @Override
    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public List<Suggestion> execute() {
        check(statement.getSubject(), SUBJECT);
        check(statement.getPredicate(), PREDICATE);
        if (statement.getResource().isResource()) {
            check(statement.getObject().asResource(), OBJECT);
        }
        return suggestions;
    }

    private void check(Resource resource, Utils.ResourceType resourceType) {
        // move some responsibilities from LocalVocabularyManager to this class here? TODO
        localVocabularyManager.checkForMatches(resource, resourceType).forEach(msg -> suggestions.add(new Suggestion(msg)));
    }
}
