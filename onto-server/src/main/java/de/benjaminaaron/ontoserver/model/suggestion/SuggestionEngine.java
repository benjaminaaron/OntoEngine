package de.benjaminaaron.ontoserver.model.suggestion;

import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.model.suggestion.job.CompareOneToAllStatementsJob;
import de.benjaminaaron.ontoserver.model.suggestion.job.PoolUniqueUrisAndTheirWordsJob;
import de.benjaminaaron.ontoserver.model.suggestion.job.Job;
import de.benjaminaaron.ontoserver.model.suggestion.job.task.CaseSensitivityTask;
import de.benjaminaaron.ontoserver.routing.websocket.WebSocketRouting;
import lombok.SneakyThrows;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@EnableAsync
public class SuggestionEngine {

    private final Logger logger = LogManager.getLogger(SuggestionEngine.class);
    private int counter = 0;
    private final Map<Integer, Suggestion> suggestions = new HashMap<>();

    @Autowired
    private WebSocketRouting router;

    @Autowired
    private ModelController modelController;

    @SneakyThrows
    @PostConstruct
    void init() {
        TaskSchedulingManager taskManager = new TaskSchedulingManager(this);
        taskManager.schedulePeriodicJob("poolUniqueUrisAndTheirWordsJob", 5, 10);
    }

    public void poolUniqueUrisAndTheirWordsJob() {
        logger.info("Starting PoolUniqueUrisAndTheirWordsJob");
        PoolUniqueUrisAndTheirWordsJob job = new PoolUniqueUrisAndTheirWordsJob(modelController.getModel());
        job.execute();
    }

    public void sendUnsentSuggestions() {
        List<Suggestion> unsent = suggestions.values().stream().filter(s -> !s.getIsSent()).collect(Collectors.toList());
        for (Suggestion sug : unsent) {
            System.out.println(sug + " sent");
            router.sendSuggestion(sug.getMessage());
            sug.isSent();
        }
    }

    @SneakyThrows
    @Async
    public void startPostAddStatementChecks(Model model, Statement newStatement) {
        // go through all resources directly instead of statements?
        Job job = new CompareOneToAllStatementsJob(model, newStatement);
        job.addTask(new CaseSensitivityTask(newStatement));
        List<Suggestion> suggestions = job.execute();
        for (Suggestion sug : suggestions) {
            registerSuggestion(sug);
        }
        Thread.sleep(3000);
        sendUnsentSuggestions();
    }

    private void registerSuggestion(Suggestion suggestion) {
        int id = ++ counter;
        suggestion.setId(id);
        suggestions.put(id, suggestion);
    }
}
