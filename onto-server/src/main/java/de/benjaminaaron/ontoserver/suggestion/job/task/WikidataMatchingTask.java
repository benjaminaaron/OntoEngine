package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.Utils.ResourceType;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.VocabularySuggestionMessage;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.FixedBasicApiConnection;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobStatementTask;
import org.apache.jena.rdf.model.Resource;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WikidataMatchingTask extends JobStatementTask {

    public static void main(String[] args) {
        WikibaseDataFetcher wbdf = new WikibaseDataFetcher(
                FixedBasicApiConnection.getWikidataApiConnection(),
                Datamodel.SITE_WIKIDATA);
        // EntityDocument q42 = wbdf.getEntityDocument("Q42");
        try {
            List<WbSearchEntitiesResult> search = wbdf.searchEntities("Berlin", "en", 5L);
            search.forEach(System.out::println);
        } catch (MediaWikiApiErrorException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void check(Resource resource, ResourceType resourceType) {
        List<VocabularySuggestionMessage> list = new ArrayList<>();

        // TODO

        list.forEach(msg -> suggestions.add(new Suggestion(msg)));
    }
}
