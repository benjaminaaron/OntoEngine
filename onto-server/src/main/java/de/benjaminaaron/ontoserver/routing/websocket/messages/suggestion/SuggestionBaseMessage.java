package de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion;

import lombok.Data;

@Data
public abstract class SuggestionBaseMessage {
    private int suggestionId;
    private String reason;
}
