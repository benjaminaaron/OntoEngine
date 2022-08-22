package de.benjaminaaron.ontoengine.suggestion.job;

import de.benjaminaaron.ontoengine.model.ModelController;
import de.benjaminaaron.ontoengine.suggestion.Suggestion;

import java.util.List;

public class PeriodicJob extends Job {

    private final ModelController modelController;

    public PeriodicJob(ModelController modelController) {
        this.modelController = modelController;
    }

    @Override
    public List<Suggestion> execute() {
        start();
        tasks.forEach(task -> {
            task.setMainModel(modelController.getMainModel());
            task.setGraphManager(modelController.getGraphManager());
            suggestions.addAll(task.execute());
        });
        stop();
        return suggestions;
    }
}
