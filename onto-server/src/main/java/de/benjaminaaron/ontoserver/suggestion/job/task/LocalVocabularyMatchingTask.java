package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.Utils.ResourceType;
import de.benjaminaaron.ontoserver.suggestion.LocalVocabularyManager;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobStatementTask;
import org.apache.jena.rdf.model.Resource;

public class LocalVocabularyMatchingTask extends JobStatementTask {

    private final LocalVocabularyManager localVocabularyManager;

    public LocalVocabularyMatchingTask(LocalVocabularyManager localVocabularyManager) {
        this.localVocabularyManager = localVocabularyManager;
    }

    @Override
    protected void check(Resource resource, ResourceType resourceType) {
        // move some responsibilities from LocalVocabularyManager to this class here? TODO
        localVocabularyManager.checkForMatches(resource, resourceType).forEach(msg -> suggestions.add(new Suggestion(msg)));
    }
}
