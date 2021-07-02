package de.benjaminaaron.ontoserver.suggestion.job;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import org.apache.jena.rdf.model.*;

import java.util.*;

public class MergeSuggestionsJob extends Job {

    private final Model mainModel;

    public MergeSuggestionsJob(Model mainModel) {
        this.mainModel = mainModel;
    }

    @Override
    public List<Suggestion> execute() {
        start();
        tasks.forEach(task -> {
            task.setMainModel(mainModel);
            suggestions.addAll(task.execute());
        });
        stop();
        return suggestions;
    }
}
