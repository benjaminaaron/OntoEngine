package de.benjaminaaron.ontoserver.model.io;

import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RawTriple {

    private final String subject;
    private final String predicate;
    private String object = "";

    public RawTriple(String subject, String predicate) {
        this.subject = subject;
        this.predicate = predicate;
    }

    public RawTriple(String subject, String predicate, String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public void appendToObject(String objectStringPart) {
        object += objectStringPart.trim() + " "; // System.getProperty("line.separator");
    }

    public String getObject() {
        return object.trim();
    }

    public List<String> getObjectParams() {
        object = object.trim();
        if (object.startsWith("\"")) object = object.substring(1);
        if (object.endsWith("\"")) object = object.substring(0, object.length() - 1);
        return Arrays.stream(object.split(",")).map(String::trim).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "subject: [" + subject + "], predicate: [" + predicate + "], object: [" + object + "]";
    }
}
