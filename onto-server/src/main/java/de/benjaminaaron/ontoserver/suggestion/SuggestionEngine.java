package de.benjaminaaron.ontoserver.suggestion;

import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.model.Utils;
import de.benjaminaaron.ontoserver.routing.websocket.WebSocketRouting;
import de.benjaminaaron.ontoserver.suggestion.job.PeriodicJob;
import de.benjaminaaron.ontoserver.suggestion.job.NewStatementJob;
import de.benjaminaaron.ontoserver.suggestion.job.task.CaseSensitivityTask;
import de.benjaminaaron.ontoserver.suggestion.job.task.LocalVocabularyMatchingTask;
import de.benjaminaaron.ontoserver.suggestion.job.task.WikidataMatchingTask;
import lombok.SneakyThrows;
import org.apache.jena.rdf.model.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
// @EnableAsync
public class SuggestionEngine {

    private final Logger logger = LogManager.getLogger(SuggestionEngine.class);
    private final Map<String, Suggestion> suggestions = new HashMap<>();

    @Autowired
    private WebSocketRouting router;

    @Autowired
    private ModelController modelController;
    private TaskSchedulingManager taskManager;

    @SneakyThrows
    @PostConstruct
    void init() {
        taskManager = new TaskSchedulingManager(this);
        taskManager.schedulePeriodicJob("runPeriodicJob", 5, 30);
    }

    public void runPeriodicJob() {
        PeriodicJob job = new PeriodicJob(modelController.getMainModel());
        job.addTask(new CaseSensitivityTask());
        handleNewSuggestions(job.execute());
    }

    public void runNewStatementJob(Statement statement, LocalVocabularyManager localVocabularyManager) {
        NewStatementJob job = new NewStatementJob(statement);
        job.addTask(new LocalVocabularyMatchingTask(localVocabularyManager));
        job.addTask(new WikidataMatchingTask());
        job.getFuture().whenComplete((_suggestions, ex) -> handleNewSuggestions(_suggestions));
        taskManager.scheduleOneTimeJobNow(job);
    }

    public void handleNewSuggestions(List<Suggestion> _suggestions) {
        _suggestions.forEach(this::registerSuggestionIfNew);
        suggestions.values().stream().filter(s -> !s.getIsSent()).forEach(suggestion -> {
            router.sendSuggestion(suggestion.getMessage());
            suggestion.markAsSent();
        });
    }

    private void registerSuggestionIfNew(Suggestion suggestion) {
        if (suggestions.containsValue(suggestion)) {
            // more refined logic required to decide when a new suggestion should replace an old one TODO
            return;
        }
        String id = Utils.generateRandomId();
        suggestion.setId(id);
        suggestions.put(id, suggestion);
    }

    public boolean suggestionExists(String id) {
        return suggestions.containsKey(id);
    }

    public String accept(String id) {
        return suggestions.get(id).getMessage().getAchievingCommand();
    }
}
