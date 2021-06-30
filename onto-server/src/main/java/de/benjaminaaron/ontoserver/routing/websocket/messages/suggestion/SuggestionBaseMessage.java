package de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion;

import lombok.Data;

@Data
public abstract class SuggestionBaseMessage {
    private String suggestionId;
    private String info;
    private String achievingCommand;
}
