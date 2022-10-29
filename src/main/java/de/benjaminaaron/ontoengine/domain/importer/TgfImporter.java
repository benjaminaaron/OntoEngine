package de.benjaminaaron.ontoengine.domain.importer;

import static de.benjaminaaron.ontoengine.domain.Utils.getFromAbsolutePathOrResolveWithinDir;

import de.benjaminaaron.ontoengine.domain.ModelController;
import java.nio.file.Path;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

// TGF = trivial graph format
public class TgfImporter {

  private static final Logger logger = LogManager.getLogger(TgfImporter.class);

  private static Path IMPORT_DIRECTORY;

  @Value("${model.import.directory}")
  public void setImportDirectory(Path dir) {
    TgfImporter.IMPORT_DIRECTORY = dir;
  }

  @SneakyThrows
  public static void doImport(ModelController modelController, String pathOrFilename) {
    Path path = getFromAbsolutePathOrResolveWithinDir(pathOrFilename, IMPORT_DIRECTORY);
    if (Objects.isNull(path)) {
      logger.warn("No file found at " + pathOrFilename);
      return;
    }

    // TODO
  }
}
