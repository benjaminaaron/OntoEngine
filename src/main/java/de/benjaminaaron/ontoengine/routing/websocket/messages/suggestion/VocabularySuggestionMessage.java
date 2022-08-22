package de.benjaminaaron.ontoengine.routing.websocket.messages.suggestion;

import de.benjaminaaron.ontoengine.model.Utils.ResourceType;
import lombok.Data;

@Data
public class VocabularySuggestionMessage extends SuggestionBaseMessage {
    private ResourceType resourceType;
    private String currentUri;
    private String suggestedUri;
}
