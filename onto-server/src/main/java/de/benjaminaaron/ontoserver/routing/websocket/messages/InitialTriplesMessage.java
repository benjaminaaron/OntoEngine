package de.benjaminaaron.ontoserver.routing.websocket.messages;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InitialTriplesMessage {
    List<AddStatementMessage> triples = new ArrayList<>();
}
