package de.benjaminaaron.ontoserver.suggestion;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.ScheduledMethodRunnable;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class TaskSchedulingManager extends ThreadPoolTaskScheduler {

    private final Logger logger = LogManager.getLogger(TaskSchedulingManager.class);
    private final Map<String, ScheduledFuture<?>> scheduledJobs = new IdentityHashMap<>();
    private final SuggestionEngine suggestionEngine;

    public TaskSchedulingManager(SuggestionEngine suggestionEngine) {
        super();
        initialize();
        this.suggestionEngine = suggestionEngine;
    }

    @SneakyThrows
    public void schedulePeriodicJob(String methodName, int startDelayInSeconds, int intervalInSeconds) {
        scheduleAtFixedRate(
                new ScheduledMethodRunnable(suggestionEngine, methodName),
                Instant.now().plusSeconds(startDelayInSeconds),
                Duration.ofSeconds(intervalInSeconds));
    }

    public void cancelJobGracefully(String jobName) {
        scheduledJobs.get(jobName).cancel(false);
        scheduledJobs.remove(jobName);
    }

    @Override
    // via https://stackoverflow.com/a/44694210/2474159
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
        String methodName = ((ScheduledMethodRunnable) task).getMethod().getName();
        logger.info("Scheduled job '" + methodName + "' with startTime " + startTime + " and period " + (period / 1000) + "s");
        ScheduledFuture<?> future = super.scheduleAtFixedRate(task, startTime, period);
        scheduledJobs.put(methodName, future);
        return future;
    }
}
