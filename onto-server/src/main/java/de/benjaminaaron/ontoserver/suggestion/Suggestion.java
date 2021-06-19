package de.benjaminaaron.ontoserver.suggestion;

import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.SuggestionBaseMessage;

public class Suggestion {

    private int id;
    private SuggestionBaseMessage message;
    private boolean isSent = false;

    public Suggestion(SuggestionBaseMessage message) {
        this.message = message;
    }

    public SuggestionBaseMessage getMessage() {
        return message;
    }

    public void setId(int id) {
        this.id = id;
        message.setSuggestionId(id);
    }

    public int getId() {
        return id;
    }

    public boolean getIsSent() {
        return isSent;
    }

    public void isSent() {
        this.isSent = true;
    }

    @Override
    public String toString() {
        return "Suggestion " + id;
    }
}
