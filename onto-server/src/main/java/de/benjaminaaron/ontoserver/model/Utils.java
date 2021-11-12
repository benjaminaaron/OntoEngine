package de.benjaminaaron.ontoserver.model;

import com.github.slugify.Slugify;
import de.benjaminaaron.ontoserver.routing.websocket.messages.TripleMessage;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class Utils {

    public static String DEFAULT_URI_NAMESPACE;
    public static String DEFAULT_URI_SEPARATOR;
    private final static Slugify slugifier = new Slugify().withLowerCase(false);

    public static String buildDefaultNsUri(String word) {
        return DEFAULT_URI_NAMESPACE + DEFAULT_URI_SEPARATOR + slugifier.slugify(word);
    }

    public static String ensureUri(String str) {
        // str = full URI or just local name (= word)
        if (isValidUri(str)) {
            return str;
        }
        return buildDefaultNsUri(str);
    }

    public static boolean isValidUri(String str) {
        try {
            new URL(str).toURI();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }

    public static boolean containsOnlyValidUris(Set<String> set) {
        for (String str : set) {
            if (!isValidUri(str)) {
                return false;
            }
        }
        return true;
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

    public static String getValueFromLiteral(Literal literal) {
        // is there a more elegant way to get the value?
        String literalStr = literal.toString();
        if (literalStr.contains("http")) {
            String[] parts = literalStr.split("http");
            return parts[0].substring(0, parts[0].length() - 2); // -2 for the ^^
        }
        return literalStr;
    }

    public static String rdfNodeToGraphDatabaseEntryString(RDFNode rdfNode) {
        if (rdfNode.isResource()) {
            return "<" + rdfNode.asResource().getURI() + ">";
        }
        Literal literal = rdfNode.asLiteral();
        if (literal.getDatatype().getJavaClass() == String.class) {
            return "\"" + literal + "\"";
        }
        return "\"" + getValueFromLiteral(literal) + "\"^^<" + literal.getDatatypeURI() + ">";
    }

    public static String getValueFromRdfNode(RDFNode rdfNode, boolean fullUri) {
        if (rdfNode.isResource()) {
            return fullUri ? rdfNode.asResource().getURI() : rdfNode.asResource().getLocalName();
        }
        if (rdfNode.isLiteral()) {
            return fullUri ? rdfNodeToGraphDatabaseEntryString(rdfNode) : getValueFromLiteral(rdfNode.asLiteral());
        }
        return rdfNode.toString();
    }

    public static String setToCompactArrayString(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        for (String entry : set) {
            sb.append(entry).append(",");
        }
        String str = sb.toString();
        return str.substring(0, sb.length() - 1);
    }

    public static String generateRandomId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static TripleMessage buildTripleMessageFromStatement(Statement statement) {
        TripleMessage triple = new TripleMessage();
        triple.setSubjectUri(statement.getSubject().getURI());
        triple.setPredicateUri(statement.getPredicate().getURI());
        if (statement.getObject().isLiteral()) {
            triple.setObjectUriOrLiteralValue(Utils.getValueFromLiteral(statement.getObject().asLiteral()));
            triple.setObjectIsLiteral(true);
        } else {
            triple.setObjectUriOrLiteralValue(statement.getObject().asResource().getURI());
            triple.setObjectIsLiteral(false);
        }
        return triple;
    }

    public enum ResourceType {
        SUBJECT, PREDICATE, OBJECT;

        public Resource fromStatement(Statement statement) {
            switch (this) {
                case SUBJECT:
                    return statement.getSubject();
                case PREDICATE:
                    return statement.getPredicate();
                case OBJECT:
                    return statement.getObject().isResource() ? statement.getObject().asResource() : null;
                default:
                    return null;
            }
        }
    }
}
