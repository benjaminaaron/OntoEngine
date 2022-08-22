package de.benjaminaaron.ontoengine.routing.websocket.messages;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InitialTriplesMessage {
    List<TripleMessage> triples = new ArrayList<>();
}
