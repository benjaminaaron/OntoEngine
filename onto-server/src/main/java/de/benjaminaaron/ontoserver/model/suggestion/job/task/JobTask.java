package de.benjaminaaron.ontoserver.model.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.suggestion.Suggestion;

import java.util.Collections;
import java.util.List;

public abstract class JobTask {
    public List<Suggestion> collectSuggestions() {
        return Collections.emptyList();
    }
}
