package de.benjaminaaron.ontoserver.model.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.ReformulateUriSuggestionMessage;
import org.apache.jena.rdf.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class CaseSensitivityTask extends JobTask {

    private final String sWord;
    private final String pWord;
    private String oWord = null;
    private final ReformulateUriSuggestionMessage sMessage;
    private final ReformulateUriSuggestionMessage pMessage;
    private ReformulateUriSuggestionMessage oMessage = null;

    public CaseSensitivityTask(Statement newStatement) {
        sWord = newStatement.getSubject().getLocalName();
        sMessage = new ReformulateUriSuggestionMessage();
        sMessage.setCurrentUri(newStatement.getSubject().getURI());

        pWord = newStatement.getPredicate().getLocalName();
        pMessage = new ReformulateUriSuggestionMessage();
        pMessage.setCurrentUri(newStatement.getPredicate().getURI());

        if (newStatement.getObject().isResource()) {
            oWord = newStatement.getObject().asResource().getLocalName();
            oMessage = new ReformulateUriSuggestionMessage();
            oMessage.setCurrentUri(newStatement.getObject().asResource().getURI());
        }
    }

    public void evaluateStatement(Statement existingStmt) {
        String sWordEx = existingStmt.getSubject().getLocalName();
        String pWordEx = existingStmt.getPredicate().getLocalName();
        if (oWord != null && existingStmt.getObject().isResource()) {
            String oWordEx = existingStmt.getObject().asResource().getLocalName();
            if (match(sWord, oWordEx)) sMessage.getSuggestedUris().add(existingStmt.getObject().asResource().getURI());
            if (match(pWord, oWordEx)) pMessage.getSuggestedUris().add(existingStmt.getObject().asResource().getURI());
            if (match(oWord, sWordEx)) oMessage.getSuggestedUris().add(existingStmt.getSubject().getURI());
            if (match(oWord, pWordEx)) oMessage.getSuggestedUris().add(existingStmt.getPredicate().getURI());
            if (match(oWord, oWordEx)) oMessage.getSuggestedUris().add(existingStmt.getObject().asResource().getURI());
        }

        if (match(sWord, sWordEx)) sMessage.getSuggestedUris().add(existingStmt.getSubject().getURI());
        if (match(sWord, pWordEx)) sMessage.getSuggestedUris().add(existingStmt.getPredicate().getURI());

        if (match(pWord, sWordEx)) pMessage.getSuggestedUris().add(existingStmt.getSubject().getURI());
        if (match(pWord, pWordEx)) pMessage.getSuggestedUris().add(existingStmt.getPredicate().getURI());
    }

    public List<Suggestion> collectSuggestions() {
        List<Suggestion> list = new ArrayList<>();
        addIfNotEmpty(list, sMessage);
        addIfNotEmpty(list, pMessage);
        addIfNotEmpty(list, oMessage);
        return list;
    }

    private boolean match(String word, String wordEx) {
        return !word.equals(wordEx) && word.equalsIgnoreCase(wordEx);
    }

    private void addIfNotEmpty(List<Suggestion> list, ReformulateUriSuggestionMessage message) {
        if (!message.getSuggestedUris().isEmpty()) {
            // Suggestion sug = new Suggestion();
            // sug.setMessage(message);
            // list.add(sug);
        }
    }
}
