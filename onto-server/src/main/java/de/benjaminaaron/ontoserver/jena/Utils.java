package de.benjaminaaron.ontoserver.jena;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Utils {

    final static String DEFAULT_BASE_URL = "http://onto/";

    public static String ensureIri(String word) {
        word = word.trim();
        try {
            new URL(word).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            return DEFAULT_BASE_URL + word;
        }
        return word;
    }
}
