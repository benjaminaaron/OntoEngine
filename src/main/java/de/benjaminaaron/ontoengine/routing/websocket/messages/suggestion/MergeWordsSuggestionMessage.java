package de.benjaminaaron.ontoengine.routing.websocket.messages.suggestion;

import lombok.Data;

import java.util.Map;

@Data
public class MergeWordsSuggestionMessage extends SuggestionBaseMessage {
    Map<String, Integer> urisToMergeAndTheirTotalUsage;
    String suggestedUri;
}
