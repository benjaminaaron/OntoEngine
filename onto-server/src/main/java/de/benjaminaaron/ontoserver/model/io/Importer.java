package de.benjaminaaron.ontoserver.model.io;

import de.benjaminaaron.ontoserver.model.ModelController;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Importer {

    @Value("${graphdb.get-url}")
    private String GRAPHDB_GET_URL;

    @Autowired
    private ModelController modelController;

    public void importFromGraphDB(String repository) {
        Model model = modelController.getModel();
        try (RDFConnection conn = RDFConnectionFactory.connect(GRAPHDB_GET_URL.replace("<repository>", repository))) {
            Txn.executeRead(conn, () -> {
                String queryStr = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }"; // LIMIT 5
                conn.querySelect(QueryFactory.create(queryStr), qs -> {
                    Resource subj = qs.getResource("s");
                    Property pred = model.createProperty(qs.get("p").toString());
                    RDFNode obj = qs.get("o");
                    modelController.addStatement(ResourceFactory.createStatement(subj, pred, obj));
                });
            });
        }
    }
}
