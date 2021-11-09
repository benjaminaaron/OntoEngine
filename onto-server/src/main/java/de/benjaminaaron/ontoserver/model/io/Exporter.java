package de.benjaminaaron.ontoserver.model.io;

import de.benjaminaaron.ontoserver.model.ModelController;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.benjaminaaron.ontoserver.model.Utils.getExportFile;
import static de.benjaminaaron.ontoserver.model.Utils.rdfNodeToGraphDatabaseEntryString;

@Component
public class Exporter {

    @Value("${model.export.directory}")
    private Path EXPORT_DIRECTORY;
    @Value("${graphdb.insert-url}")
    private String GRAPHDB_INSERT_URL;
    @Value("classpath:graphdb_repo_template.json")
    private Path GRAPHDB_REPO_TEMPLATE;
    @Value("${graphdb.rest-url}")
    private String GRAPHDB_REST_URL;
    @Value("${graphdb.default-repository}")
    private String GRAPHDB_DEFAULT_REPOSITORY;

    @Autowired
    private ModelController modelController;

    public void exportRDF(String modelName) {
        Model model = null;
        String extension = "rdf";
        switch (modelName) {
            case "main":
                model = modelController.getMainModel();
                break;
            case "meta":
                model = modelController.getMetaHandler().getMetaDataModel();
                extension = "owl";
                break;
        }
        try(FileOutputStream fos = new FileOutputStream(getExportFile(EXPORT_DIRECTORY, modelName, extension))) {
            assert model != null;
            model.write(fos, "RDF/XML");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportGraphml(boolean fullUri) {
        modelController.getGraph().exportGraphml(getExportFile(EXPORT_DIRECTORY, "model", "graphml"), fullUri);
    }

    @SneakyThrows
    public void exportToGraphDB(String modelName) {
        // allow passing of a "clear" flag TODO
        String repoName = GRAPHDB_DEFAULT_REPOSITORY + (modelName.equals("meta") ? "-meta" : "");
        String ruleset = "rdfsplus-optimized"; // empty
        String insertURL = GRAPHDB_INSERT_URL.replace("<repository>", repoName);

        // delete old repo

        URL url = new URL(insertURL);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("DELETE");
        http.setRequestProperty("Accept", "application/json");
        if (http.getResponseCode() != 204) {
            System.out.println("Could not delete the GraphDB repository");
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
            System.out.println("Could not create GraphDB repository");
        }
        http.disconnect();

        // add triples to repo

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
