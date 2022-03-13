package de.benjaminaaron.ontoserver.model.io;

import de.benjaminaaron.ontoserver.model.MetaHandler.StatementOrigin;
import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.model.Utils;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementMessage;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;

@Component
public class Importer {

    @Value("${graphdb.get-url}")
    private String GRAPHDB_GET_URL;
    @Value("${markdown.export.directory}")
    private Path MARKDOWN_DEFAULT_DIRECTORY;

    @Autowired
    private ModelController modelController;

    @SneakyThrows
    public void importFromMarkdown() {
        Path markdownDir = Utils.getObsidianICloudDir(); // MARKDOWN_DEFAULT_DIRECTORY
        Model model = modelController.getMainModel();
        Map<String, Path> markdownFiles = new HashMap<>(); // key: resourceLocalName, value: file path
        Map<String, String> filenamesToUris = new HashMap<>();

        // import prefixes.md if existent
        Optional<Path> prefixFileOptional = Files.walk(markdownDir).filter(Files::isRegularFile).filter(path -> !path.toString().contains(".Trash"))
                .filter(path -> path.getFileName().toString().equalsIgnoreCase("prefixes.md")).findAny();
        if (prefixFileOptional.isPresent()) {
            Files.lines(prefixFileOptional.get()).forEach(line -> {
                String prefix = line.split(":")[0];
                String uri = line.substring(prefix.length() + 1);
                model.setNsPrefix(prefix, uri);
            });
        }

        // collect markdown files and URIs of resources
        Files.walk(markdownDir).filter(Files::isRegularFile).filter(path -> !path.toString().contains(".Trash"))
                .filter(path -> !path.getFileName().toString().equalsIgnoreCase("prefixes.md"))
                .filter(path -> FilenameUtils.isExtension(path.toString(), "md"))
                .forEach(path -> {
                    String filename = FilenameUtils.getBaseName(path.getFileName().toString()); // = localName of resource
                    markdownFiles.put(filename, path);
                    try (Stream<String> stream = Files.lines(path)) {
                        // means it can be anywhere in the file - restrict its possible location more?
                        Optional<String> uriDefOptional = stream.filter(line -> !line.isBlank())
                                .filter(line -> line.split(" ").length == 1).findAny();
                        if (uriDefOptional.isPresent()) {
                            String uriDef = uriDefOptional.get().trim();
                            String uri = uriDef.startsWith("http") ? uriDef : model.getNsPrefixURI(uriDef.split(":")[0].trim()) + filename;
                            filenamesToUris.put(filename, uri);
                        } else {
                            filenamesToUris.put(filename, Utils.buildDefaultNsUri(filename));
                        }
                    } catch (IOException ignored) {}
                });

        // run through markdown files to import statements
        markdownFiles.forEach((localName, path) -> {
            try (Stream<String> stream = Files.lines(path)) {
                stream.filter(line -> line.trim().split(" ").length >= 2).forEach(line -> {
                    String predicateUri = Utils.expandShortUriRepresentation(line.split(" ")[0].trim(), model);
                    String object = line.substring(line.split(" ")[0].length() + 1).trim();
                    AddStatementMessage statement = new AddStatementMessage();
                    statement.setSubject(filenamesToUris.get(localName));
                    statement.setPredicate(predicateUri);
                    statement.setObjectIsLiteral(object.startsWith("\""));
                    if (statement.isObjectIsLiteral()) {
                        statement.setObject(object.substring(1, object.length() - 1));
                    } else {
                        if (object.startsWith("[[")) {
                            object = object.substring(2, object.length() - 2); // remove the [[]]
                        }
                        statement.setObject(filenamesToUris.containsKey(object)
                                ? filenamesToUris.get(object) : Utils.buildDefaultNsUri(object));
                    }
                    modelController.addStatement(statement);
                });
            } catch (IOException ignored) {}
        });
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
