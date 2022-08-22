package de.benjaminaaron.ontoengine.model.dataset;

import org.apache.jena.query.Dataset;

public interface DatasetProvider {

    Dataset getDataset();
}
