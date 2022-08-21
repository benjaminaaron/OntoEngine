package de.benjaminaaron.ontoserver.model.dataset;

import org.apache.jena.query.Dataset;

public interface DatasetProvider {

    Dataset getDataset();
}
