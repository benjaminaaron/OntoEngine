package de.benjaminaaron.ontoengine.model.dataset;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetConfig {

    private final Path TBD_DIR;

    public DatasetConfig(@Value("${jena.tdb.directory}") Path TBD_DIR) {
        this.TBD_DIR = TBD_DIR;
    }

    @Bean
    public DatasetProvider getDatasetProvider() {
        return new TbdDatasetProvider(TBD_DIR);
    }
}
