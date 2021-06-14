package de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public
class ReformulateUriSuggestionMessage extends SuggestionBaseMessage {
    private String currentUri;
    private Set<String> suggestedUris = new HashSet<>();
}
