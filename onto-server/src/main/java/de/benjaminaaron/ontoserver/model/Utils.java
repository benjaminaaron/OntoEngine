package de.benjaminaaron.ontoserver.model;

import org.apache.jena.datatypes.RDFDatatype;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    final static String DEFAULT_NAMESPACE = "http://onto.de/";

    public static String ensureUri(String fullUriOrJustLocalName) {
        fullUriOrJustLocalName = fullUriOrJustLocalName.trim();
        try {
            new URL(fullUriOrJustLocalName).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            return DEFAULT_NAMESPACE + fullUriOrJustLocalName;
        }
        return fullUriOrJustLocalName;
    }

    public static String pathFromUri(String uri) {
        // TODO doesn't work with multiple "/"s
        return URI.create(uri).getPath().substring(1);
    }

    public static String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    }

    public static File getExportFile(Path exportPath, String baseName, String extension) {
        exportPath.toFile().mkdirs();
        return exportPath.resolve(baseName + "_" + getTimestamp() + "." + extension).toFile();
    }

    public static Object detectLiteralType(String data) {
        if (data.equalsIgnoreCase("true") || data.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(data);
        }
        try {
            return Integer.parseInt(data);
        } catch (NumberFormatException ignored) {}
        try {
            return Double.parseDouble(data);
        } catch (NumberFormatException ignored) {}
        // TODO more types
        return data;
    }
}
