package de.benjaminaaron.ontoengine.domain.importer;

import static de.benjaminaaron.ontoengine.domain.MetaHandler.StatementOrigin.RDF_IMPORT;
import static de.benjaminaaron.ontoengine.domain.Utils.getFromAbsolutePathOrResolveWithinDir;

import de.benjaminaaron.ontoengine.domain.ModelController;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RdfImporter {

    private static final Logger logger = LogManager.getLogger(RdfImporter.class);

    private static Path IMPORT_DIRECTORY;

    @Value("${model.import.directory}")
    public void setImportDirectory(Path dir) {
        RdfImporter.IMPORT_DIRECTORY = dir;
    }

    @SneakyThrows
    public static void doImport(ModelController modelController, String pathOrFilename) {
        // check if path is an absolute path, otherwise append it to IMPORT_DIRECTORY
        Path path = getFromAbsolutePathOrResolveWithinDir(pathOrFilename, IMPORT_DIRECTORY);
        if (Objects.isNull(path)) {
            logger.warn("No file found at " + pathOrFilename +
                ", either provide absolute path or filename within " + IMPORT_DIRECTORY.toAbsolutePath());
            return;
        }
        Model importModel = ModelFactory.createDefaultModel();
        importModel.read(path.toString()); // via https://jena.apache.org/documentation/io/rdf-input.html
        StmtIterator iter = importModel.listStatements();
        while (iter.hasNext()) {
            modelController.addStatement(iter.nextStatement(), RDF_IMPORT, pathOrFilename,
                null, false);
        }

        Path imported = IMPORT_DIRECTORY.resolve("imported");
        imported.toFile().mkdirs();
        Files.move(path, imported.resolve(path.getFileName()));
        logger.info("Import from RDF/TTL file completed");
    }

    private static JsonObject statementToJsonObj(Statement statement) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("subject", statement.getSubject().toString());
        jsonObject.put("predicate", statement.getPredicate().toString());
        jsonObject.put("object", statement.getObject().toString());
        return jsonObject;
    }

    @SneakyThrows
    public static JsonObject doImportFromInputStream(ModelController modelController, String fileName, InputStream inputStream) {
        Model importModel = ModelFactory.createDefaultModel();
        String lang = FilenameUtils.getExtension(fileName).equalsIgnoreCase("rdf") ? "RDF" : "TTL";
        importModel.read(inputStream, null, lang);
        StmtIterator iter = importModel.listStatements();
        JsonArray imported = new JsonArray();
        JsonArray alreadyPresent = new JsonArray();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            if (modelController.statementAlreadyPresent(stmt)) {
                alreadyPresent.add(statementToJsonObj(stmt));
                continue;
            }
            modelController.addStatement(stmt, RDF_IMPORT, fileName, null, false);
            imported.add(statementToJsonObj(stmt));
        }
        logger.info("Import of uploaded RDF/TTL file completed");
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.put("imported", imported);
        jsonResponse.put("alreadyPresent", alreadyPresent);
        return jsonResponse;
    }
}
