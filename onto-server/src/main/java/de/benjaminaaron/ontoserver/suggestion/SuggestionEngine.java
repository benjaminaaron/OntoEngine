package de.benjaminaaron.ontoserver.suggestion;

import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.suggestion.job.MergeSuggestionsJob;
import de.benjaminaaron.ontoserver.routing.websocket.WebSocketRouting;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
        MergeSuggestionsJob job = new MergeSuggestionsJob(modelController.getModel());
        job.execute().forEach(this::registerSuggestion);
        sendUnsentSuggestions();
    }

    public void sendUnsentSuggestions() {
        List<Suggestion> unsent = suggestions.values().stream().filter(s -> !s.getIsSent()).collect(Collectors.toList());
        for (Suggestion sug : unsent) {
            System.out.println(sug + " sent");
            router.sendSuggestion(sug.getMessage());
            sug.isSent();
        }
    }

    private void registerSuggestion(Suggestion suggestion) {
        int id = ++ counter;
        suggestion.setId(id);
        suggestions.put(id, suggestion);
    }
}
