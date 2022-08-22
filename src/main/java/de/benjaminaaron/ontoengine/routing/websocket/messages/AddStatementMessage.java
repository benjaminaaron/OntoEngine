package de.benjaminaaron.ontoengine.routing.websocket.messages;

import lombok.Data;

@Data
public class AddStatementMessage {
    private String subject;
    private String predicate;
    private String object;
    private boolean objectIsLiteral;
}
