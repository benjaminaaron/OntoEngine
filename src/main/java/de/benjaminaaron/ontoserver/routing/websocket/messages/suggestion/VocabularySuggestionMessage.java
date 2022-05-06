package de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion;

import de.benjaminaaron.ontoserver.model.Utils.ResourceType;
import lombok.Data;

@Data
public class VocabularySuggestionMessage extends SuggestionBaseMessage {
    private ResourceType resourceType;
    private String currentUri;
    private String suggestedUri;
}
