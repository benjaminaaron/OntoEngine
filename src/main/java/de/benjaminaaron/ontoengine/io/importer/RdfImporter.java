package de.benjaminaaron.ontoengine.io.importer;

import static de.benjaminaaron.ontoengine.model.MetaHandler.StatementOrigin.RDF_IMPORT;
import static de.benjaminaaron.ontoengine.model.Utils.getFromAbsolutePathOrResolveWithinDir;

import de.benjaminaaron.ontoengine.model.ModelController;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
        logger.info("Import from RDF file completed");
    }
}
