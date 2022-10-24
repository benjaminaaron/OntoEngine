package de.benjaminaaron.ontoengine.adapter.primary.messages.suggestion;

import de.benjaminaaron.ontoengine.model.Utils.ResourceType;
import lombok.Data;

@Data
public class ExternalMatchMessage extends SuggestionBaseMessage {
    private ResourceType resourceType;
    private String externalSource;
    private String currentUri;
    private String matchUri;
    private String matchLabel;
}
