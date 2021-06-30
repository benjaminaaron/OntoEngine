package de.benjaminaaron.ontoserver.suggestion.job;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import lombok.SneakyThrows;
import org.apache.jena.rdf.model.Model;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class RunnableJob extends Job implements Runnable {

    private final static int THREAD_SLEEP_MILLIS = 3000;
    private final CompletableFuture<List<Suggestion>> future = new CompletableFuture<>();

    public RunnableJob(Model model) {
        super(model);
    }

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
