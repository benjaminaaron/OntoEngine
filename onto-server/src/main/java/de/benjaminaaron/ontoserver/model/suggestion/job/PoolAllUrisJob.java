package de.benjaminaaron.ontoserver.model.suggestion.job;

import de.benjaminaaron.ontoserver.model.suggestion.Suggestion;
import org.apache.jena.rdf.model.Model;

import java.util.List;

public class PoolAllUrisJob extends Job {

    public PoolAllUrisJob(Model model) {
        super(model);
    }

    @Override
    public List<Suggestion> execute() {
        startTimer();

        // TODO

        endTimer();
        System.out.println(getJobDurationString());
        return null;
    }
}
