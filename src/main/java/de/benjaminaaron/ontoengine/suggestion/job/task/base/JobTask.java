package de.benjaminaaron.ontoengine.suggestion.job.task.base;

import de.benjaminaaron.ontoengine.model.graph.GraphManager;
import de.benjaminaaron.ontoengine.suggestion.Suggestion;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

import java.util.ArrayList;
import java.util.List;

public abstract class JobTask {

    protected List<Suggestion> suggestions = new ArrayList<>();

    public abstract List<Suggestion> execute();

    public void setMainModel(Model mainModel) {}

    public void setStatement(Statement statement) {}

    public void setGraphManager(GraphManager graphManager) {}
}
