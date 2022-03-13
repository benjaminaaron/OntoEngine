package de.benjaminaaron.ontoserver.model.io;

import de.benjaminaaron.ontoserver.model.MetaHandler.StatementOrigin;
import de.benjaminaaron.ontoserver.model.ModelController;
import lombok.SneakyThrows;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public class Importer {

    @Value("${graphdb.get-url}")
    private String GRAPHDB_GET_URL;
    @Value("${markdown.export.directory}")
    private Path MARKDOWN_DIRECTORY;

    @Autowired
    private ModelController modelController;

    @SneakyThrows
    public void importFromMarkdown() {
        try (Stream<Path> paths = Files.walk(MARKDOWN_DIRECTORY)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                String filename = file.getFileName().toString();
                if (filename.toLowerCase().endsWith(".md")) {
                    try (Stream<String> lines = Files.lines(file)) {
                        lines.forEach(line -> {
                            // TODO
                        });
                    } catch (IOException ignored) {}
                }
            });
        }
    }

    public void importFromGraphDB(String repository) {
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
                            ResourceFactory.createStatement(subj, pred, obj), StatementOrigin.IMPORT, repoUrl, null);
                });
            });
        }
    }
}
