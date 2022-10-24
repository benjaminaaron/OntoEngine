package de.benjaminaaron.ontoengine.adapter.primary.messages;

import lombok.Data;

@Data
public class AddStatementMessage {
    private String subject;
    private String predicate;
    private String object;
    private boolean objectIsLiteral;
}
