package de.benjaminaaron.ontoserver.model.io;

import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.model.Utils;
import de.benjaminaaron.ontoserver.model.graph.Edge;
import lombok.SneakyThrows;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.jgrapht.Graph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.FileWriter;
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
    @Value("${markdown.export.directory}")
    private Path MARKDOWN_DIRECTORY;
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
        modelController.getGraphManager().exportGraphml(getExportFile(EXPORT_DIRECTORY, "model", "graphml"), fullUri);
    }

    public void exportMarkdown() {
        MARKDOWN_DIRECTORY.toFile().mkdirs();
        Graph<RDFNode, Edge> graph = modelController.getGraphManager().getGraph();
        for (RDFNode node : graph.vertexSet()) {
            if (node.isLiteral()) {
                continue;
            }
            Resource source = node.asResource();
            Path newFile = MARKDOWN_DIRECTORY.resolve(source.getLocalName() + ".md");
            try(FileWriter fw = new FileWriter(newFile.toFile())) {
                Utils.writeLine(fw, source.getURI());
                Utils.writeLine(fw, "");
                for (Edge edge : graph.outgoingEdgesOf(node)) {
                    RDFNode target = graph.getEdgeTarget(edge);
                    Utils.writeLine(fw,
                            edge.property.getURI() + " " +
                            (target.isResource() ?
                                    "[[" + target.asResource().getLocalName() + "]]"
                                    :
                                    target.asLiteral().toString())
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SneakyThrows
    public void exportToGraphDB(String modelName, String ruleset) {
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
