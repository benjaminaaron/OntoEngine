package de.benjaminaaron.ontoserver.model;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Utils {

    final static String DEFAULT_BASE_URL = "http://onto/";

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
}
