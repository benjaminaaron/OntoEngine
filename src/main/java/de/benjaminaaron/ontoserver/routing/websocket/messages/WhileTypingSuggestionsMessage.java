package de.benjaminaaron.ontoserver.routing.websocket.messages;

import de.benjaminaaron.ontoserver.model.Utils.ResourceType;
import lombok.Data;

import java.util.Map;

@Data
public class WhileTypingSuggestionsMessage {
    private ResourceType resourceType;
    private String value;
    private Map<String, String> matches;
}
