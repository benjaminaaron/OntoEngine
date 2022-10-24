package de.benjaminaaron.ontoengine.domain.suggestion.job.task.base;

import org.apache.jena.rdf.model.Model;

public abstract class JobModelTask extends JobTask {

    protected Model mainModel;

    @Override
    public void setMainModel(Model mainModel) {
        this.mainModel = mainModel;
    }
}
