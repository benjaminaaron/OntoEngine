package de.benjaminaaron.ontoengine.suggestion.job.task;

import de.benjaminaaron.ontoengine.model.Utils.ResourceType;
import de.benjaminaaron.ontoengine.adapter.primary.messages.suggestion.ExternalMatchMessage;
import de.benjaminaaron.ontoengine.suggestion.Suggestion;
import de.benjaminaaron.ontoengine.suggestion.job.task.base.JobStatementTask;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;
import org.wikidata.wdtk.wikibaseapi.WbGetEntitiesSearchData;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.benjaminaaron.ontoengine.model.Utils.ResourceType.PREDICATE;

public class WikidataMatchingTask extends JobStatementTask {

    private final WikibaseDataFetcher wbdf;

    public WikidataMatchingTask() {
        // removed FixedBasicApiConnection.java when upgrading to
        // org.wikidata.wdtk:wdtk-wikibaseapi:0.14.1 from 0.12.1
        // verify this works TODO
        wbdf = new WikibaseDataFetcher(
                BasicApiConnection.getWikidataApiConnection(),
                Datamodel.SITE_WIKIDATA);
        // https://www.wikidata.org/wiki/Wikidata:Tools/For_programmers
        // https://www.mediawiki.org/wiki/Wikibase/Indexing/RDF_Dump_Format#Full_list_of_prefixes
        // https://www.wikidata.org/wiki/Help:Basic_membership_properties
        // https://www.wikidata.org/wiki/Wikidata:SPARQL_tutorial
    }

    @Override
    protected void check(Statement statement, ResourceType resourceType) {
        Resource resource = resourceType.fromStatement(statement);
        List<ExternalMatchMessage> list = new ArrayList<>();
        WbGetEntitiesSearchData searchProps = new WbGetEntitiesSearchData();
        searchProps.language = "en";
        searchProps.search = resource.getLocalName();
        searchProps.limit = 1L;
        searchProps.type = PREDICATE == resourceType ? "property" : "item";
        try {
            List<WbSearchEntitiesResult> search = wbdf.searchEntities(searchProps);
            search.stream()
                    .filter(result -> result.getLabel().equalsIgnoreCase(resource.getLocalName()))
                    .forEach(result -> {
                ExternalMatchMessage message = new ExternalMatchMessage();
                message.setTaskName(getClass().getSimpleName());
                message.setExternalSource("Wikidata");
                message.setCurrentUri(resource.getURI());
                message.setResourceType(resourceType);
                message.setMatchUri(result.getConceptUri());
                message.setMatchLabel(result.getLabel());
                message.setInfo("Description: " + result.getDescription());
                // TODO
                // String command = "REPLACE " + resource.getURI() + " WITH " + result.getConceptUri() + " / " +
                //         "LABEL " + result.getConceptUri() + " AS '" + result.getLabel() + "' / " +
                //         "ADDTRIPLE " + resource.getURI() + " " + Utils.buildDefaultNsUri("sameAs") + " " + result.getConceptUri();
                message.setAchievingCommand("TODO");
                list.add(message);
            });
        } catch (MediaWikiApiErrorException | IOException e) {
            System.err.println("Failed to run search on Wikidata for " + resource.getLocalName());
        }
        list.forEach(msg -> suggestions.add(new Suggestion(msg)));
    }
}
