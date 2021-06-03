package de.benjaminaaron.ontoserver.routing;

import de.benjaminaaron.ontoserver.jena.JenaController;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseRouting {

    @Autowired
    protected JenaController jenaController;

}
