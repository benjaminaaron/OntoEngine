package de.benjaminaaron.ontoengine.adapter.primary.messages.suggestion;

import de.benjaminaaron.ontoengine.model.Utils.ResourceType;
import lombok.Data;

@Data
public class VocabularySuggestionMessage extends SuggestionBaseMessage {
    private ResourceType resourceType;
    private String currentUri;
    private String suggestedUri;
}
