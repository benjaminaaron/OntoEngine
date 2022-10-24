package de.benjaminaaron.ontoengine.domain.dataset;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

public class InMemoryDatasetProvider extends DatasetProvider {

    private final Dataset dataset;

    public InMemoryDatasetProvider() {
        dataset = DatasetFactory.createTxnMem();
    }

    @Override
    public Dataset getDataset() {
        return dataset;
    }
}
