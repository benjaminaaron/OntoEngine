package de.benjaminaaron.ontoengine.model.io;

import static de.benjaminaaron.ontoengine.model.Utils.NEW_LINE_CHAR;

import lombok.Data;
import org.apache.jena.atlas.lib.Pair;

import java.util.ArrayList;
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
        object += objectStringPart + " " + NEW_LINE_CHAR;
    }

    public String getObject() {
        return object;
    }

    public void cleanObject() {
        object = object.trim();
        if (object.startsWith("\"")) object = object.substring(1);
        if (object.endsWith("\"")) object = object.substring(0, object.length() - 1);
    }

    public List<String> getObjectParamsCSV() {
        return Arrays.stream(object.split(",")).map(String::trim).collect(Collectors.toList());
    }

    public Pair<List<RawTriple>, List<RawTriple>> getObjectParamsIFTTT() {
        // A father B mother C --> A grandmother C
        String[] whereParts = object.split("-->")[0].trim().split(" ");
        String[] constructParts = object.split("-->")[1].trim().split(" ");
        return Pair.create(collectIftttTriples(whereParts), collectIftttTriples(constructParts));
    }

    private List<RawTriple> collectIftttTriples(String[] parts) {
        List<RawTriple> list = new ArrayList<>();
        int sub = 0;
        // A father B mother C
        for (int i = 0; i < (parts.length - 1) / 2; i++) {
            int idx = i * 3 - (sub ++);
            list.add(new RawTriple(parts[idx], parts[idx + 1], parts[idx + 2]));
        }
        // <A father B> <B mother C>
        return list;
    }

    public String toQueryLine() {
        return "?" + subject + " :" + predicate + " ?" + object + " . ";
    }

    public String toValuesQueryLine(int predIdx) {
        String predVar = "?predVar" + predIdx;
        String queryLine = "?" + subject + " " + predVar + " ?" + object + " . VALUES " + predVar + " { " ;
        for (String value : predicate.split("/")) {
            queryLine += ":" + value + " ";
        }
        return queryLine + " } . ";
    }

    @Override
    public String toString() {
        return "subject: [" + subject + "], predicate: [" + predicate + "], object: [" + object + "]";
    }
}
