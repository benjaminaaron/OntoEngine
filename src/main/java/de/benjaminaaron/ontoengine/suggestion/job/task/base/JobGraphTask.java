package de.benjaminaaron.ontoengine.suggestion.job.task.base;

import de.benjaminaaron.ontoengine.model.graph.GraphManager;

public abstract class JobGraphTask extends JobTask {

    protected GraphManager graphManager;

    public void setGraphManager(GraphManager graphManager) {
        this.graphManager = graphManager;
    }
}
