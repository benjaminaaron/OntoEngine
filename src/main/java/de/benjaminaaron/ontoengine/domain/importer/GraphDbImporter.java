package de.benjaminaaron.ontoengine.domain.importer;

import static de.benjaminaaron.ontoengine.domain.MetaHandler.StatementOrigin.GRAPHDB_IMPORT;

import de.benjaminaaron.ontoengine.domain.ModelController;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GraphDbImporter {

    private static final Logger logger = LogManager.getLogger(GraphDbImporter.class);

    private static String GRAPHDB_GET_URL;

    @Value("${graphdb.get-url}")
    public void setGraphDbGetUrl(String url) {
        GraphDbImporter.GRAPHDB_GET_URL = url;
    }

    public static void doImport(ModelController modelController, String repository) {
        Model mainModel = modelController.getMainModel();
        String repoUrl = GRAPHDB_GET_URL.replace("<repository>", repository);
        try (RDFConnection conn = RDFConnectionFactory.connect(repoUrl)) {
            Txn.executeRead(conn, () -> {
                String queryStr = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }"; // LIMIT 5
                conn.querySelect(QueryFactory.create(queryStr), qs -> {
                    Resource subj = qs.getResource("s");
                    Property pred = mainModel.createProperty(qs.get("p").toString());
                    RDFNode obj = qs.get("o");
                    modelController.addStatement(
                        ResourceFactory.createStatement(subj, pred, obj), GRAPHDB_IMPORT,
                        repoUrl, null, false);
                });
            });
        }
        // TODO count and log how many statements were read vs. actually added
        logger.info("Import from GraphDB completed");
    }
}
