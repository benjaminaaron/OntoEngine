package de.benjaminaaron.ontoengine.routing.websocket.messages;

import lombok.Data;

@Data
public class AddStatementResponse {
    private boolean statementAdded = false;
    private boolean subjectIsNew = false;
    private boolean predicateIsNew = false;
    private boolean objectIsNew = false;
    @Override
    public String toString() {
        return "statementAdded: " + statementAdded + ", subjectIsNew: " + subjectIsNew +
                ", predicateIsNew: " + predicateIsNew + ", objectIsNew: " + objectIsNew;
    }
}
