package de.benjaminaaron.ontoengine.suggestion.job.task;

import de.benjaminaaron.ontoengine.model.Utils.ResourceType;
import de.benjaminaaron.ontoengine.suggestion.LocalVocabularyManager;
import de.benjaminaaron.ontoengine.suggestion.Suggestion;
import de.benjaminaaron.ontoengine.suggestion.job.task.base.JobStatementTask;
import org.apache.jena.rdf.model.Statement;

public class LocalVocabularyMatchingTask extends JobStatementTask {

    private final LocalVocabularyManager localVocabularyManager;

    public LocalVocabularyMatchingTask(LocalVocabularyManager localVocabularyManager) {
        this.localVocabularyManager = localVocabularyManager;
    }

    @Override
    protected void check(Statement statement, ResourceType resourceType) {
        // move some responsibilities from LocalVocabularyManager to this class here? TODO
        localVocabularyManager.checkForMatches(statement, resourceType).forEach(msg -> suggestions.add(new Suggestion(msg)));
    }
}
