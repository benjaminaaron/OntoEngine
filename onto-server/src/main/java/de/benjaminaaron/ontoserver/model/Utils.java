package de.benjaminaaron.ontoserver.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    final static String DEFAULT_BASE_URL = "http://onto.de/";

    public static String ensureUri(String word) {
        word = word.trim();
        try {
            new URL(word).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            return DEFAULT_BASE_URL + word;
        }
        return word;
    }

    public static String pathFromUri(String uri) {
        return URI.create(uri).getPath().substring(1);
    }

    public static String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    }

    public static File getExportFile(Path exportPath, String baseName, String extension) {
        exportPath.toFile().mkdirs();
        return exportPath.resolve(baseName + "_" + getTimestamp() + "." + extension).toFile();
    }
}
