package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.Utils.ResourceType;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.VocabularySuggestionMessage;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobStatementTask;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class WikidataMatchingTask extends JobStatementTask {

    @Override
    protected void check(Resource resource, ResourceType resourceType) {
        List<VocabularySuggestionMessage> list = new ArrayList<>();

        // TODO

        list.forEach(msg -> suggestions.add(new Suggestion(msg)));
    }
}
