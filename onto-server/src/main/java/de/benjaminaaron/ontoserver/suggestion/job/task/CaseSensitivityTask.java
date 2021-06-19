package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.MergeWordsSuggestionMessage;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.UriStats;

import java.util.*;

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
            message.setUrisToMergeAndTheirTotalUsage(urisWithStats);
            message.setSuggestedUri(uriMaxUsed);

            suggestions.add(new Suggestion(message));
        });
    }

}
