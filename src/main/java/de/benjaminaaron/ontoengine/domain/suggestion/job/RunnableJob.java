package de.benjaminaaron.ontoengine.domain.suggestion.job;

import de.benjaminaaron.ontoengine.domain.suggestion.Suggestion;
import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class RunnableJob extends Job implements Runnable {

    private final static int THREAD_SLEEP_MILLIS = 3000;
    private final CompletableFuture<List<Suggestion>> future = new CompletableFuture<>();

    public CompletableFuture<List<Suggestion>> getFuture() {
        return future;
    }

    @SneakyThrows
    @Override
    public void run() {
        Thread.sleep(THREAD_SLEEP_MILLIS);
        future.complete(execute());
    }
}
