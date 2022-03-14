package de.benjaminaaron.ontoserver.model.io;

import de.benjaminaaron.ontoserver.model.MetaHandler.StatementOrigin;
import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoserver.suggestion.Query;
import de.benjaminaaron.ontoserver.suggestion.SuggestionEngine;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.benjaminaaron.ontoserver.model.Utils.*;
import static de.benjaminaaron.ontoserver.suggestion.Query.QueryType.PERIODIC;
import static de.benjaminaaron.ontoserver.suggestion.Query.QueryType.parse;
import static org.apache.commons.io.FilenameUtils.getBaseName;

@Component
public class Importer {

    @Value("${graphdb.get-url}")
    private String GRAPHDB_GET_URL;
    @Value("${markdown.export.directory}")
    private Path MARKDOWN_DEFAULT_DIRECTORY;

    @Autowired
    private ModelController modelController;

    @Autowired
    private SuggestionEngine suggestionEngine;

    @SneakyThrows
    public void importFromMarkdown() {
        Path markdownDir = getObsidianICloudDir(); // MARKDOWN_DEFAULT_DIRECTORY
        Model model = modelController.getMainModel();
        Map<String, Path> markdownFiles = new HashMap<>(); // key: resourceLocalName, value: file path
        Map<String, String> filenamesToUris = new HashMap<>();

        // import PREFIXES.md if existent
        Optional<Path> prefixesFileOptional = getSpecialMarkdownFile(markdownDir, "PREFIXES.md");
        if (prefixesFileOptional.isPresent()) {
            Files.lines(prefixesFileOptional.get()).forEach(line -> {
                String prefix = line.split(":")[0];
                String uri = line.substring(prefix.length() + 1);
                model.setNsPrefix(prefix, uri);
            });
        }

        // process QUERIES.md if existent
        // make it more robust than all this if/else and line splitting? TODO
        // handle URI-expansion TODO
        Optional<Path> queriesFileOptional = getSpecialMarkdownFile(markdownDir, "QUERIES.md");
        if (queriesFileOptional.isPresent()) {
            List<String> lines = Files.lines(queriesFileOptional.get())
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.trim().startsWith("//"))
                    .collect(Collectors.toList());
            boolean withinQuery = false;
            String queryStr = "";
            String queryName = "";
            String queryTypeStr = "";
            for (String line : lines) {
                if (withinQuery) {
                    if (line.trim().equals("\"")) {
                        if (!queryStr.isEmpty()) {
                            withinQuery = false;
                            queryStr = queryStr.trim();
                            Query query = new Query();
                            query.setQuery(queryStr);
                            query.setQueryName(queryName);
                            query.setType(parse(queryTypeStr));
                            suggestionEngine.addQuery(query);
                            queryStr = "";
                        }
                    } else { // building the query
                        queryStr += line + " " + System.getProperty("line.separator");
                    }
                } else {
                    if (line.split(" ").length == 2) { // query definition
                        withinQuery = true;
                        queryName = line.split(" ")[0].trim();
                        queryTypeStr = line.split(" ")[1].trim();
                    }
                    if (line.split(" ").length > 2) { // template instantiation command
                        String command = line.split(" ")[0];
                        queryName = line.split(" ")[1];
                        String paramsStr = line.split("\"")[1];
                        List<String> params = Arrays.stream(paramsStr.split(",")).map(String::trim).collect(Collectors.toList());
                        Optional<Query> template = suggestionEngine.getTemplateQuery(queryName);
                        if (template.isPresent()) {
                            String queryStrReplaced = template.get().getQuery();
                            for (int i = 0; i < params.size(); i++) {
                                String param = params.get(i);
                                int varIdx = i + 1;
                                queryName += "_" + param;
                                queryStrReplaced = queryStrReplaced.replaceAll("<var" + varIdx + ">", ":" + param);
                            }
                            Query query = new Query();
                            query.setQuery(queryStrReplaced);
                            query.setQueryName(queryName);
                            query.setType(PERIODIC);
                            suggestionEngine.addQuery(query);
                        }
                    }
                }
            }
        }

        // collect markdown files and URIs of resources
        getNormalMarkdownFiles(markdownDir)
                .forEach(path -> {
                    String filename = getBaseName(path.getFileName().toString()); // = localName of resource
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
                            filenamesToUris.put(filename, buildDefaultNsUri(filename));
                        }
                    } catch (IOException ignored) {}
                });

        // run through markdown files to import statements
        markdownFiles.forEach((localName, path) -> {
            try (Stream<String> stream = Files.lines(path)) {
                stream.filter(line -> line.trim().split(" ").length >= 2).forEach(line -> {
                    String predicateUri = expandShortUriRepresentation(line.split(" ")[0].trim(), model);
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
                                ? filenamesToUris.get(object) : buildDefaultNsUri(object));
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
