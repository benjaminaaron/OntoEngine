package de.benjaminaaron.ontoengine.domain.exporter;

import static de.benjaminaaron.ontoengine.domain.Utils.getExportFile;

import de.benjaminaaron.ontoengine.domain.ModelController;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RdfExporter {

    private static Path EXPORT_DIRECTORY;

    @Value("${model.export.directory}")
    public void setExportDirectory(Path dir) {
        RdfExporter.EXPORT_DIRECTORY = dir;
    }

    public static Path export(ModelController modelController, String modelName) {
        Model model = null;
        String extension = "ttl";
        switch (modelName) {
            case "main":
                model = modelController.getMainModel();
                break;
            case "meta":
                model = modelController.getMetaHandler().getMetaDataModel();
                extension = "owl";
                break;
        }
        File exportFile = getExportFile(EXPORT_DIRECTORY, modelName, extension);
        try (FileOutputStream fos = new FileOutputStream(exportFile)) {
            assert model != null;
            model.write(fos, "TURTLE"); // RDF/XML, via https://jena.apache.org/documentation/io/rdf-output.html
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exportFile.toPath();
    }
}
