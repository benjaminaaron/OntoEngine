package de.benjaminaaron.ontoengine.adapter.primary.messages.suggestion;

import de.benjaminaaron.ontoengine.domain.Utils.ResourceType;
import lombok.Data;

@Data
public class ExternalMatchMessage extends SuggestionBaseMessage {
    private ResourceType resourceType;
    private String externalSource;
    private String currentUri;
    private String matchUri;
    private String matchLabel;
}
