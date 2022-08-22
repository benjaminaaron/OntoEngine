package de.benjaminaaron.ontoengine.model.dataset;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetConfig {

    @Bean
    public DatasetProvider getDatasetProvider() {
        return new InMemoryDatasetProvider();
    }
}
