package de.benjaminaaron.ontoengine.adapter.primary.messages;

import de.benjaminaaron.ontoengine.domain.Utils.ResourceType;
import lombok.Data;

import java.util.Map;

@Data
public class WhileTypingSuggestionsMessage {
    private ResourceType resourceType;
    private String value;
    private Map<String, String> matches;
}
