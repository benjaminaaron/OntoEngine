package de.benjaminaaron.ontoengine.adapter.primary.messages;

import java.net.URL;
import lombok.Data;

@Data
public class ProjectCreationInfo {
    String id; // slugified name
    String name;
    boolean useTdb;
    URL externalEndpoint;
    boolean useMetaModel;
}
