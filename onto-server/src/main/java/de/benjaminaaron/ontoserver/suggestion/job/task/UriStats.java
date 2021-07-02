package de.benjaminaaron.ontoserver.suggestion.job.task;

public class UriStats {
    public String uri;
    public String word;
    int usedAsSubject = 0;
    int usedAsPredicate = 0;
    int usedAsObject = 0;

    public UriStats(String uri, String word) {
        this.uri = uri;
        this.word = word;
    }

    public int getTotalUsed() {
        return usedAsSubject + usedAsPredicate + usedAsObject;
    }

    @Override
    public String toString() {
        return uri + ": " + usedAsSubject + ", " + usedAsPredicate + ", " + usedAsObject;
    }
}
