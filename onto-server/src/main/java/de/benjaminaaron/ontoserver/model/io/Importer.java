package de.benjaminaaron.ontoserver.model.io;

import de.benjaminaaron.ontoserver.model.MetaHandler;
import de.benjaminaaron.ontoserver.model.MetaHandler.StatementOrigin;
import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoserver.suggestion.SuggestionEngine;
import lombok.SneakyThrows;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import static org.apache.commons.io.FilenameUtils.getBaseName;

@Component
public class Importer {

    private final Logger logger = LogManager.getLogger(Importer.class);

    @Value("${graphdb.get-url}")
    private String GRAPHDB_GET_URL;
    @Value("${markdown.export.directory}")
    private Path MARKDOWN_DEFAULT_DIRECTORY;

    @Autowired
    private ModelController modelController;

    @Autowired
    private SuggestionEngine suggestionEngine;

    @Autowired
    private MetaHandler metaHandler;

    @SneakyThrows
    public void importFromMarkdown(String folderName) {
        Path markdownDir = getObsidianICloudDir(folderName);
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
        // handle URI-expansion TODO
        Optional<Path> queriesFileOptional = getSpecialMarkdownFile(markdownDir, "QUERIES.md");
        if (queriesFileOptional.isPresent()) {
            for (RawTriple triple : parseTriples(queriesFileOptional.get())) {
                switch (triple.getPredicate()) {
                    case "hasPeriodicQueryTemplate":
                    case "hasPeriodicQuery":
                        metaHandler.storeQueryTriple(triple.getSubject(), triple.getPredicate(), triple.getObject());
                        break;
                    case "instantiatePeriodicQueryTemplateFor":
                        String templateQueryStr = metaHandler.getPeriodicQueryTemplate(ensureUri(triple.getSubject()));
                        if (Objects.isNull(templateQueryStr)) {
                            logger.warn("No template query found for \"" + triple.getSubject() + "\", could not instantiate query for \"" + triple.getSubject() + "\"");
                            break;
                        }
                        String instantiatedQueryName = triple.getSubject();
                        String queryStrReplaced = templateQueryStr;
                        List<String> params = triple.getObjectParamsCSV();
                        for (int i = 0; i < params.size(); i++) {
                            String param = params.get(i);
                            instantiatedQueryName += "_" + param;
                            queryStrReplaced = queryStrReplaced.replaceAll("<var" + (i + 1) + ">", ":" + param);
                        }
                        metaHandler.storeInstantiatedTemplateQueryTriple(
                                instantiatedQueryName, ensureUri("hasPeriodicQuery"), queryStrReplaced, triple.getSubject());
                        break;
                    case "ifttt":
                        Pair<List<RawTriple>, List<RawTriple>> parts = triple.getObjectParamsIFTTT();
                        List<RawTriple> whereParts = parts.getLeft();
                        List<RawTriple> constructParts = parts.getRight();
                        int valuesPredCount = 0;
                        String query =
                                "PREFIX : <http://onto.de/default#> " +
                                "CONSTRUCT { ";
                        for (RawTriple cpart : constructParts) {
                            query += cpart.toQueryLine();
                        }
                        query += "} WHERE { ";
                        for (RawTriple wpart : whereParts) {
                            if (wpart.getPredicate().contains("/")) {
                                query += wpart.toValuesQueryLine(valuesPredCount ++);
                            } else {
                                query += wpart.toQueryLine();
                            }
                        }
                        query += "}";
                        metaHandler.storeIFTTTtriple(triple.getSubject(), "hasPeriodicQuery", query, triple.getObject());
                        break;
                    default:
                        break;
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

    private List<RawTriple> parseTriples(Path path) throws IOException {
        List<String> lines = Files.lines(path)
                .filter(line -> !line.isBlank()) // ignore empty lines
                .filter(line -> !line.trim().startsWith("//")) // ignore comments
                .collect(Collectors.toList());
        List<RawTriple> triples = new ArrayList<>();
        boolean inObjectString = false;
        for (String line : lines) {
            if (line.trim().equals("\"")) {
                inObjectString = !inObjectString;
                continue;
            }
            if (inObjectString) {
                triples.get(triples.size() - 1).appendToObject(line); // make safer TODO
                continue;
            }
            String[] parts = line.split(" ");
            if (parts.length == 2) { // object starts in next line
                triples.add(new RawTriple(parts[0], parts[1]));
            }
            if (parts.length > 2) { // full triple in one line
                triples.add(new RawTriple(parts[0], parts[1], line.substring(parts[0].length() + parts[1].length() + 2)));
            }
        }
        triples.forEach(RawTriple::cleanObject);
        return triples;
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
