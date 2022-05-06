package de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion;

import lombok.Data;

@Data
public class SuggestionBaseMessage {
    private String taskName; // of the task producing this suggestion
    private String suggestionId;
    private String info;
    private String achievingCommand;

    // use lombok serialisation instead? TODO
    public String toBasicString() {
        return "Task: " + taskName + ", Info: " + info;
    }
}
