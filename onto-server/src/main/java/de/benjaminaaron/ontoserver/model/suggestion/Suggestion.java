package de.benjaminaaron.ontoserver.model.suggestion;

import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.ReformulateUriSuggestionMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.SuggestionBaseMessage;

public class Suggestion {

    private int id;
    private ReformulateUriSuggestionMessage message; // TODO use SuggestionBaseMessage
    private boolean isSent = false;

    public void setMessage(SuggestionBaseMessage message) {
        this.message = (ReformulateUriSuggestionMessage) message;
    }

    public ReformulateUriSuggestionMessage getMessage() {
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
        return "Suggestion " + id + ": " + message.getCurrentUri() + " --> " + message.getSuggestedUris();
    }
}
