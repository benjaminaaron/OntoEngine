package de.benjaminaaron.ontoengine.model.dataset;

import java.nio.file.Path;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;

public class TbdDatasetProvider extends DatasetProvider {

    private final Dataset dataset;

    public TbdDatasetProvider(Path TBD_DIR) {
        dataset = TDBFactory.createDataset(TBD_DIR.toString());
    }

    @Override
    public Dataset getDataset() {
        return dataset;
    }
}
