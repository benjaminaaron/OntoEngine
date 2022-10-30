package de.benjaminaaron.ontoengine.domain.importer;

import static de.benjaminaaron.ontoengine.domain.Utils.getFromAbsolutePathOrResolveWithinDir;

import de.benjaminaaron.ontoengine.adapter.primary.messages.AddStatementMessage;
import de.benjaminaaron.ontoengine.domain.ModelController;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// TGF = trivial graph format
@Component
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
    List<String> lines = Files.lines(path).collect(Collectors.toList());
    boolean inEdges = false;
    Map<Integer, String> nodes = new HashMap<>();
    for (String line : lines) {
      if (line.startsWith("#")) {
        inEdges = true;
        continue;
      }
      if (inEdges) {
        String subject = nodes.get(Integer.parseInt(line.split(" ")[0]));
        String object = nodes.get(Integer.parseInt(line.split(" ")[1]));
        String predicate = line.substring(line.split(" ")[0].length() +
            line.split(" ")[1].length() + 2);
        AddStatementMessage message = new AddStatementMessage();
        message.setSubject(subject);
        message.setPredicate(predicate);
        message.setObject(object);
        message.setObjectIsLiteral(false);
        modelController.addStatement(message, true);
      } else {
        nodes.put(Integer.parseInt(line.split(" ")[0]),
            line.substring(line.split(" ")[0].length() + 1));
      }
    }
  }
}
