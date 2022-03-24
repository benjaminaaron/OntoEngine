package de.benjaminaaron.ontoserver.model.io;

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
        object += objectStringPart + System.getProperty("line.separator");
    }

    @Override
    public String toString() {
        return "subject: [" + subject + "], predicate: [" + predicate + "], object: [" + object + "]";
    }
}
