package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.Utils;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.MergeWordsSuggestionMessage;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobModelTask;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.UriStats;
import org.apache.jena.rdf.model.*;

import java.util.*;

public class CaseSensitivityTask extends JobModelTask {

    @Override
    public List<Suggestion> execute() {
        Map<String, UriStats> map = collect();
        Map<String, Set<String>> buckets = new HashMap<>();
        map.forEach((uri, stats) -> {
            String key = stats.word.toLowerCase();
            buckets.putIfAbsent(key, new HashSet<>());
            buckets.get(key).add(uri);
        });

        Map<String, Set<String>> multiBuckets = new HashMap<>();
        buckets.forEach((key, uris) -> {
            if (uris.size() > 1) {
                multiBuckets.put(key, uris);
            }
        });

        multiBuckets.forEach((key, uris) -> {
            MergeWordsSuggestionMessage message = new MergeWordsSuggestionMessage();
            Map<String, Integer> urisWithStats = new HashMap<>();
            int maxUsed = 0;
            String uriMaxUsed = null;
            for (String uri : uris) {
                int used = map.get(uri).getTotalUsed();
                urisWithStats.put(uri, used);
                if (used > maxUsed) {
                    maxUsed = used;
                    uriMaxUsed = uri;
                }
            }
            final String suggestedMergeUri = uriMaxUsed;
            message.setUrisToMergeAndTheirTotalUsage(urisWithStats);
            message.setSuggestedUri(suggestedMergeUri);
            Set<String> set = urisWithStats.keySet();
            set.remove(suggestedMergeUri);
            message.setAchievingCommand("REPLACE " + Utils.setToCompactArrayString(set)
                    + " WITH " + suggestedMergeUri);
            message.setInfo("These URIs were found to use the same word when compared case insensitive, consider merging them");
            suggestions.add(new Suggestion(message));
        });

        return suggestions;
    }

    private Map<String, UriStats> collect() {
        // ResIterator sIter = model.listSubjects();
        // ExtendedIterator<Node> pIter = GraphUtil.listPredicates(model.getGraph(), Node.ANY, Node.ANY);
        // NodeIterator oIter = model.listObjects();
        // Iter.asStream(model.getGraph().find(null, null, null)).count()
        Map<String, UriStats> map = new HashMap<>();

        StmtIterator stmtIterator = mainModel.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();

            Resource subject = statement.getSubject();
            String sUri = subject.getURI();
            map.putIfAbsent(sUri, new UriStats(sUri, subject.getLocalName()));
            map.get(sUri).usedAsSubject ++;

            Property predicate = statement.getPredicate();
            String pUri = predicate.getURI();
            map.putIfAbsent(pUri, new UriStats(pUri, predicate.getLocalName()));
            map.get(pUri).usedAsPredicate ++;

            RDFNode object = statement.getObject();
            if (object.isResource()) {
                String oUri = object.asResource().getURI();
                map.putIfAbsent(oUri, new UriStats(oUri, object.asResource().getLocalName()));
                map.get(oUri).usedAsObject ++;
            }
        }

        return map;
    }
}
