package de.benjaminaaron.ontoengine.domain.suggestion.job.task.base;

public class UriStats {
    public String uri;
    public String word;
    public int usedAsSubject = 0;
    public int usedAsPredicate = 0;
    public int usedAsObject = 0;

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
