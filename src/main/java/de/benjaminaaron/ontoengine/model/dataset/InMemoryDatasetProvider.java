package de.benjaminaaron.ontoengine.model.dataset;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

public class InMemoryDatasetProvider implements DatasetProvider {

    @Override
    public Dataset getDataset() {
        return DatasetFactory.createTxnMem();
    }
}
