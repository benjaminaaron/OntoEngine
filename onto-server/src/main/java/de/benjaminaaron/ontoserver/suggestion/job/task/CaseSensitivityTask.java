package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.Utils;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.MergeWordsSuggestionMessage;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.UriStats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CaseSensitivityTask extends JobTask {

    @Override
    public void execute(Map<String, UriStats> map) {
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
    }
}
