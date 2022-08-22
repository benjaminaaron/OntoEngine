package de.benjaminaaron.ontoengine.io.exporter;

import static de.benjaminaaron.ontoengine.model.Utils.getExportFile;

import de.benjaminaaron.ontoengine.model.ModelController;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GraphmlExporter {

    private static Path EXPORT_DIRECTORY;

    @Value("${model.export.directory}")
    public void setExportDirectory(Path dir) {
        GraphmlExporter.EXPORT_DIRECTORY = dir;
    }

    public static void export(ModelController modelController, boolean fullUri) {
        modelController.getGraphManager().exportGraphml(
            getExportFile(EXPORT_DIRECTORY, "model", "graphml"), fullUri);
    }
}
