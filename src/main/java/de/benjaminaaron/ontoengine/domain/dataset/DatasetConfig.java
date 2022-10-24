package de.benjaminaaron.ontoengine.domain.dataset;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetConfig {

    private final Path tbdDir;
    private final String mainModelName;
    private final String metaModelName;
    private final String vocabularySourcesModelName;
    private final String defaultUriNamespace;

    public DatasetConfig(
        @Value("${jena.tdb.directory}") Path tbdDir,
        @Value("${jena.tdb.model.main.name}") String mainModelName,
        @Value("${jena.tdb.model.meta.name}") String metaModelName,
        @Value("${jena.tdb.model.vocabulary-sources.name}") String vocabularySourcesModelName,
        @Value("${uri.default.namespace}") String defaultUriNamespace) {
        this.tbdDir = tbdDir;
        this.mainModelName = mainModelName;
        this.metaModelName = metaModelName;
        this.vocabularySourcesModelName = vocabularySourcesModelName;
        this.defaultUriNamespace = defaultUriNamespace;
    }

    @Bean
    public DatasetProvider getDatasetProvider() {
        TbdDatasetProvider provider = new TbdDatasetProvider(tbdDir);
        provider.setConfig(mainModelName, metaModelName, vocabularySourcesModelName, defaultUriNamespace);
        provider.init();
        return provider;
    }
}
