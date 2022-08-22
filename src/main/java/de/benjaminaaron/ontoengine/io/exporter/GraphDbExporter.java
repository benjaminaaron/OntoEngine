package de.benjaminaaron.ontoengine.io.exporter;

import static de.benjaminaaron.ontoengine.model.Utils.rdfNodeToGraphDatabaseEntryString;

import de.benjaminaaron.ontoengine.model.ModelController;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GraphDbExporter {

    private static String GRAPHDB_INSERT_URL;
    private static Path GRAPHDB_REPO_TEMPLATE;
    private static String GRAPHDB_REST_URL;
    private static String GRAPHDB_DEFAULT_REPOSITORY;

    @Autowired
    public void setValues(
        @Value("${graphdb.insert-url}") String insertUrl,
        @Value("classpath:graphdb_repo_template.json") Path repoTemplate,
        @Value("${graphdb.rest-url}") String restUrl,
        @Value("${graphdb.default-repository}") String defaultRepo) {
        GRAPHDB_INSERT_URL = insertUrl;
        GRAPHDB_REPO_TEMPLATE = repoTemplate;
        GRAPHDB_REST_URL = restUrl;
        GRAPHDB_DEFAULT_REPOSITORY = defaultRepo;
    }

    @SneakyThrows
    public static void export(ModelController modelController, String modelName, String ruleset) {
        // allow passing of a "clear" flag TODO
        String repoName = GRAPHDB_DEFAULT_REPOSITORY + "_" + modelName + "_" + ruleset;
        String insertURL = GRAPHDB_INSERT_URL.replace("<repository>", repoName);

        // delete old repo

        URL url = new URL(insertURL);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("DELETE");
        http.setRequestProperty("Accept", "application/json");
        if (http.getResponseCode() != 204) {
            System.out.println("Could not delete the GraphDB repository. Response code: " + http.getResponseCode());
        }
        http.disconnect();

        // create new repo

        url = new URL(GRAPHDB_REST_URL);
        http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Accept", "application/json");
        String jsonStr = Files.readString(GRAPHDB_REPO_TEMPLATE, StandardCharsets.UTF_8)
            .replace("<id>", repoName)
            .replace("<ruleset>", ruleset);
        http.getOutputStream().write(jsonStr.getBytes(StandardCharsets.UTF_8));
        if (http.getResponseCode() != 201) {
            System.out.println("Could not create new GraphDB repository. Response code: " + http.getResponseCode());
        }
        http.disconnect();

        // add triples to new repo

        Model model = modelName.equals("main") ? modelController.getMainModel() : modelController.getMetaModel();
        try (RDFConnection conn = RDFConnectionFactory.connect(insertURL)) {
            Txn.executeWrite(conn, () -> {
                StmtIterator iterator = model.listStatements();
                while (iterator.hasNext()) {
                    Statement statement = iterator.nextStatement();
                    String sUri = statement.getSubject().getURI();
                    String pUri = statement.getPredicate().getURI();
                    String objStr = rdfNodeToGraphDatabaseEntryString(statement.getObject());
                    conn.update("INSERT DATA { <" + sUri + "> <" + pUri + "> " + objStr + " }");
                }
            });
        }
    }
}
