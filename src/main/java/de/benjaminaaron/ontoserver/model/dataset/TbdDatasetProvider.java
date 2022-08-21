package de.benjaminaaron.ontoserver.model.dataset;

import java.nio.file.Path;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;

public class TbdDatasetProvider implements DatasetProvider {

    private final Path TBD_DIR;

    public TbdDatasetProvider(Path TBD_DIR) {
        this.TBD_DIR = TBD_DIR;
    }

    @Override
    public Dataset getDataset() {
        return TDBFactory.createDataset(TBD_DIR.toString());
    }
}
