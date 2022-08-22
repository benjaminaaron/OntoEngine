package de.benjaminaaron.ontoengine.io.importer;

import static de.benjaminaaron.ontoengine.model.Utils.buildDefaultNsUri;
import static de.benjaminaaron.ontoengine.model.Utils.ensureUri;
import static de.benjaminaaron.ontoengine.model.Utils.expandShortUriRepresentation;
import static de.benjaminaaron.ontoengine.model.Utils.getNormalMarkdownFiles;
import static de.benjaminaaron.ontoengine.model.Utils.getObsidianICloudDir;
import static de.benjaminaaron.ontoengine.model.Utils.getSpecialMarkdownFile;
import static org.apache.commons.io.FilenameUtils.getBaseName;

import de.benjaminaaron.ontoengine.model.MetaHandler;
import de.benjaminaaron.ontoengine.model.ModelController;
import de.benjaminaaron.ontoengine.routing.websocket.messages.AddStatementMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MarkdownImporter {

    private static final Logger logger = LogManager.getLogger(MarkdownImporter.class);

    private static Path MARKDOWN_DEFAULT_DIRECTORY;

    @Value("${markdown.export.directory}")
    public void setMarkdownDefaultDirectory(Path dir) {
        MarkdownImporter.MARKDOWN_DEFAULT_DIRECTORY = dir;
    }

    @SneakyThrows
    public static void doImport(ModelController modelController, String folderName) {
        Path markdownDir = getObsidianICloudDir(folderName);
        Model model = modelController.getMainModel();
        MetaHandler metaHandler = modelController.getMetaHandler();
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
                        metaHandler.storeInstantiatedTemplateQueryTriple(instantiatedQueryName,
                            ensureUri("hasPeriodicQuery"), queryStrReplaced,
                            triple.getSubject(), triple.getObject());
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
                    modelController.addStatement(statement, false);
                });
            } catch (IOException ignored) {}
        });

        // TODO count and log how many statements were read vs. actually added
        String text = "Import from markdown files completed";
        logger.info(text);
        modelController.broadcastToChangeListeners(text);
    }

    private static List<RawTriple> parseTriples(Path path) throws IOException {
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
}
