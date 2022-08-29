package de.benjaminaaron.ontoengine.model.dataset;

import de.benjaminaaron.ontoengine.model.MetaHandler;
import de.benjaminaaron.ontoengine.model.Utils;
import de.benjaminaaron.ontoengine.model.graph.GraphManager;
import javax.annotation.PreDestroy;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DatasetProvider {

    private final Logger logger = LogManager.getLogger(DatasetProvider.class);

    private Model mainModel;
    private Model metaModel;
    private Model vocabularySourcesModel;

    // this should be a @Component
    private GraphManager graphManager;

    private String mainModelName;
    private String metaModelName;
    private String vocabularySourcesModelName;
    private String defaultUriNamespace;

    public abstract Dataset getDataset();

    public void setConfig(String mainModelName, String metaModelName,
        String vocabularySourcesModelName, String defaultUriNamespace) {
        this.mainModelName = mainModelName;
        this.metaModelName = metaModelName;
        this.vocabularySourcesModelName = vocabularySourcesModelName;
        this.defaultUriNamespace = defaultUriNamespace;
    }

    void init() {
        Utils.DEFAULT_URI_NAMESPACE = defaultUriNamespace;

        // Main Model
        if (!getDataset().containsNamedModel(mainModelName)) {
            logger.info("Creating " + mainModelName + "-model in TDB location");
        }
        // these are actually what is called "named graphs" elsewhere
        // support adding/querying over entire dataset vs. specific named graphs/models TODO
        mainModel = getDataset().getNamedModel(mainModelName);
        mainModel.setNsPrefix("onto", defaultUriNamespace);
        // Meta Model
        if (!getDataset().containsNamedModel(metaModelName)) {
            logger.info("Creating " + metaModelName + "-model in TDB location");
        }
        metaModel = getDataset().getNamedModel(metaModelName);
        metaModel.setNsPrefix("meta", MetaHandler.META_NS + "#");
        // Vocabulary Sources Model
        if (!getDataset().containsNamedModel(vocabularySourcesModelName)) {
            logger.info("Creating " + vocabularySourcesModelName + "-model in TDB location");
        }
        vocabularySourcesModel = getDataset().getNamedModel(vocabularySourcesModelName);

        graphManager = new GraphManager(mainModel);
    }

    @PreDestroy
    private void close() {
        mainModel.close();
        metaModel.close();
        vocabularySourcesModel.close();
    }

    public Model getMainModel() {
        return mainModel;
    }

    public Model getMetaModel() {
        return metaModel;
    }

    public Model getVocabularySourcesModel() {
        return vocabularySourcesModel;
    }

    public GraphManager getGraphManager() {
        return graphManager;
    }
}
