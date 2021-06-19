package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.UriStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class JobTask {

    List<Suggestion> suggestions = new ArrayList<>();

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public void execute(Map<String, UriStats> map) {}
}
