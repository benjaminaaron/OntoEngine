package de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion;

import lombok.Data;

@Data
public abstract class SuggestionBaseMessage {
    private String suggestionId;
    private String reason;
    private String achievingCommand;
}
