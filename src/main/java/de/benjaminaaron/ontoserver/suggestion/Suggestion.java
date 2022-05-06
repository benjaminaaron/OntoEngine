package de.benjaminaaron.ontoserver.suggestion;

import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.MergeWordsSuggestionMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.SuggestionBaseMessage;

public class Suggestion {

    private String id;
    private final SuggestionBaseMessage message;
    private boolean isSent = false;

    public Suggestion(SuggestionBaseMessage message) {
        this.message = message;
    }

    public SuggestionBaseMessage getMessage() {
        return message;
    }

    public void setId(String id) {
        this.id = id;
        message.setSuggestionId(id);
    }

    public String getId() {
        return id;
    }

    public boolean getIsSent() {
        return isSent;
    }

    public void markAsSent() {
        this.isSent = true;
    }

    @Override
    public String toString() {
        return "Suggestion " + id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != Suggestion.class) {
            return false;
        }
        Suggestion other = (Suggestion) obj;
        if (id != null && other.getId() != null && id.equals(other.getId())) {
            return true;
        }
        if (!message.getClass().equals(other.message.getClass())) {
            return false;
        }
        if (MergeWordsSuggestionMessage.class.equals(message.getClass())) {
            MergeWordsSuggestionMessage thisMsg = (MergeWordsSuggestionMessage) message;
            MergeWordsSuggestionMessage otherMsg = (MergeWordsSuggestionMessage) other.message;
            return thisMsg.getUrisToMergeAndTheirTotalUsage().equals(otherMsg.getUrisToMergeAndTheirTotalUsage())
                    && thisMsg.getSuggestedUri().equals(otherMsg.getSuggestedUri());
        }
        return false;
    }
}
