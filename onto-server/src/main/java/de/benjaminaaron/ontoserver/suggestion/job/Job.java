package de.benjaminaaron.ontoserver.suggestion.job;

import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.SuggestionBaseMessage;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.JobTask;
import org.apache.jena.rdf.model.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Job {

    private final Logger logger = LogManager.getLogger(Job.class);

    final Model model;
    List<Suggestion> suggestions = new ArrayList<>();
    private Instant startTime;
    private Instant endTime;

    public Job(Model model) {
        this.model = model;
    }

    List<JobTask> tasks = new ArrayList<>();

    public void addTask(JobTask task) {
        tasks.add(task);
    }

    public abstract List<Suggestion> execute();

    void addSuggestionIfNotNull(SuggestionBaseMessage message) {
        if (!Objects.isNull(message)) {
            suggestions.add(new Suggestion(message));
        }
    }

    void start() {
        logger.info("Starting " + getJobName());
        startTime = Instant.now();
    }

    void stop() {
        endTime = Instant.now();
        logger.info(getJobDurationString());
    }

    public String getJobName() {
        return this.getClass().getSimpleName();
    }

    public String getJobDurationString() {
        return getJobName() + " job ran from " + startTime + " to " + endTime + ": " + Duration.between(startTime, endTime)
                + " and generated " + suggestions.size() + " new or existing suggestion" + (suggestions.size() == 1 ? "" : "s");
    }
}
