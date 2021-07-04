package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.Utils;
import de.benjaminaaron.ontoserver.model.Utils.ResourceType;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.ExternalMatchMessage;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.FixedBasicApiConnection;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobStatementTask;
import org.apache.jena.rdf.model.Resource;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.wikibaseapi.WbGetEntitiesSearchData;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.benjaminaaron.ontoserver.model.Utils.ResourceType.PREDICATE;

public class WikidataMatchingTask extends JobStatementTask {

    private final WikibaseDataFetcher wbdf;

    public WikidataMatchingTask() {
        wbdf = new WikibaseDataFetcher(
                FixedBasicApiConnection.getWikidataApiConnection(),
                Datamodel.SITE_WIKIDATA);
    }

    @Override
    protected void check(Resource resource, ResourceType resourceType) {
        List<ExternalMatchMessage> list = new ArrayList<>();
        WbGetEntitiesSearchData searchProps = new WbGetEntitiesSearchData();
        searchProps.language = "en";
        searchProps.search = resource.getLocalName();
        searchProps.limit = 5L;
        searchProps.type = PREDICATE == resourceType ? "property" : "item";
        try {
            List<WbSearchEntitiesResult> search = wbdf.searchEntities(searchProps);
            search.forEach(result -> {
                ExternalMatchMessage message = new ExternalMatchMessage();
                message.setExternalSource("Wikidata");
                message.setCurrentUri(resource.getURI());
                message.setResourceType(resourceType);
                message.setMatchUri(result.getConceptUri());
                message.setMatchLabel(result.getLabel());
                message.setInfo("Description: " + result.getDescription());
                // TODO
                String command = "REPLACE " + resource.getURI() + " WITH " + result.getConceptUri() + " / " +
                        "LABEL " + result.getConceptUri() + " AS '" + result.getLabel() + "' / " +
                        "ADDTRIPLE " + resource.getURI() + " " + Utils.buildDefaultNsUri("sameAs") + " " + result.getConceptUri();
                message.setAchievingCommand(command);
                list.add(message);
            });
        } catch (MediaWikiApiErrorException | IOException e) {
            System.err.println("Failed to run search on Wikidata for " + resource.getLocalName());
        }
        list.forEach(msg -> suggestions.add(new Suggestion(msg)));
    }
}
