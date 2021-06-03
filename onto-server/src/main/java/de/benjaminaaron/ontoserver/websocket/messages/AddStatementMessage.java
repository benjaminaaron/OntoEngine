package de.benjaminaaron.ontoserver.websocket.messages;

import lombok.Data;

@Data
public class AddStatementMessage {
    private String subject;
    private String predicate;
    private String object;
}
