package de.benjaminaaron.ontoengine.adapter.primary.messages;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InitialTriplesMessage {
    List<TripleMessage> triples = new ArrayList<>();
}
