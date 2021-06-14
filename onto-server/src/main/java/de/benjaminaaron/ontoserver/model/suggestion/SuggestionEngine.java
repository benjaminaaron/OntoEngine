package de.benjaminaaron.ontoserver.model.suggestion;

import de.benjaminaaron.ontoserver.model.suggestion.runthrough.CompareOneToAllStatementsRunThrough;
import de.benjaminaaron.ontoserver.model.suggestion.runthrough.RunThrough;
import de.benjaminaaron.ontoserver.model.suggestion.runthrough.task.CaseSensitivityTask;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SuggestionEngine {

    private int counter = 0;
    private final Map<Integer, Suggestion> suggestions = new HashMap<>();

    public void sendUnsentSuggestions() {
        List<Suggestion> unsent = suggestions.values().stream().filter(s -> !s.getIsSent()).collect(Collectors.toList());
        for (Suggestion sug : unsent) {
            System.out.println(sug);
            // TODO
            sug.isSent();
        }
    }

    public void startPostAddStatementChecks(StmtIterator iterator, Statement newStatement) {
        // go through all resources directly instead of statements?
        RunThrough runThrough = new CompareOneToAllStatementsRunThrough(iterator, newStatement);
        runThrough.addTask(new CaseSensitivityTask(newStatement));
        List<Suggestion> suggestions = runThrough.execute();
        for (Suggestion sug : suggestions) {
            registerSuggestion(sug);
        }
        sendUnsentSuggestions();
    }

    private void registerSuggestion(Suggestion suggestion) {
        int id = ++ counter;
        suggestion.setId(id);
        suggestions.put(id, suggestion);
    }
}
