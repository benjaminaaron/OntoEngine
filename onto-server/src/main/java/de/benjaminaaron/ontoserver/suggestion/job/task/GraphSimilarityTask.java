package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobGraphTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class GraphSimilarityTask extends JobGraphTask {

    private final Logger logger = LogManager.getLogger(GraphSimilarityTask.class);

    @Override
    public List<Suggestion> execute() {

        // TODO
        
        return suggestions;
    }
}
