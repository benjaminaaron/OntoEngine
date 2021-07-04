package de.benjaminaaron.ontoserver.suggestion.job;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import org.apache.jena.rdf.model.*;

import java.util.*;

public class PeriodicJob extends Job {

    private final Model mainModel;

    public PeriodicJob(Model mainModel) {
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
