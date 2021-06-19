package de.benjaminaaron.ontoserver.suggestion.job;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.JobTask;
import org.apache.jena.rdf.model.Model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public abstract class Job {

    final Model model;
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

    void startTimer() {
        startTime = Instant.now();
    }

    void endTimer() {
        endTime = Instant.now();
    }

    String getJobDurationString() {
        return "The job ran from " + startTime + " to " + endTime + ": " + Duration.between(startTime, endTime);
    }
}
