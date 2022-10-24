package de.benjaminaaron.ontoengine.domain.suggestion.job.task.base;

import de.benjaminaaron.ontoengine.domain.graph.GraphManager;

public abstract class JobGraphTask extends JobTask {

    protected GraphManager graphManager;

    public void setGraphManager(GraphManager graphManager) {
        this.graphManager = graphManager;
    }
}
