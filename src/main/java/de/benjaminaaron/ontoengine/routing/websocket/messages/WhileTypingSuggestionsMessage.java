package de.benjaminaaron.ontoengine.routing.websocket.messages;

import de.benjaminaaron.ontoengine.model.Utils.ResourceType;
import lombok.Data;

import java.util.Map;

@Data
public class WhileTypingSuggestionsMessage {
    private ResourceType resourceType;
    private String value;
    private Map<String, String> matches;
}
