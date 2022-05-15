package de.benjaminaaron.ontoserver.suggestion.job.task;

import de.benjaminaaron.ontoserver.model.graph.Edge;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.MergeWordsSuggestionMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.SuggestionBaseMessage;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import de.benjaminaaron.ontoserver.suggestion.job.task.base.JobGraphTask;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.IndexWordSet;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.jgrapht.Graph;

public class SynonymsTask extends JobGraphTask {

    @SneakyThrows
    @Override
    public List<Suggestion> execute() {
        Dictionary dictionary = Dictionary.getDefaultResourceInstance();
        Graph<RDFNode, Edge> graph = graphManager.getGraph();
        List<RDFNode> vertices = graph.vertexSet().stream().toList();
        List<Edge> edges = graph.edgeSet().stream().toList(); // TODO
        // multi buckets (like in CaseSensitivityTask) instead of just pairs? HashSet of all local names to nodes maybe... TODO

        // double-for with moving index = number of unique handshakes
        for (int i = 0; i < vertices.size() - 1; i++) {
            if (vertices.get(i).isLiteral()) continue;
            Resource vertex1 = vertices.get(i).asResource();
            IndexWordSet indexWordSet1 = dictionary.lookupAllIndexWords(vertex1.getLocalName()); // deal with camel case TODO
            for (int j = i + 1; j < vertices.size(); j++) {
                if (vertices.get(j).isLiteral()) continue;
                Resource vertex2 = vertices.get(j).asResource();
                SuggestionBaseMessage message = synonymCheck(vertex1.getLocalName(), indexWordSet1, vertex2.getLocalName());
                if (Objects.nonNull(message)) {
                    suggestions.add(new Suggestion(message));
                }
            }
        }
        return suggestions;
    }

    private SuggestionBaseMessage synonymCheck(String thisWord, IndexWordSet indexWordSet, String otherWord) {
        for (IndexWord indexWord : indexWordSet.getIndexWordCollection()) {
            for (Synset syn : indexWord.getSenses()) {
                if (syn.containsWord(otherWord)) {
                    List<String> lemmas = syn.getWords().stream().map(Word::getLemma).toList();
                    SuggestionBaseMessage message = new SuggestionBaseMessage();
                    message.setTaskName(getClass().getSimpleName());
                    message.setInfo("\"" + thisWord + "\" and \"" + otherWord + "\" (" + syn.getPOS().getLabel()
                        + ") could be synonyms: " + lemmas + ", " + syn.getGloss());
                    // use rest of MergeWordsSuggestionMessage too or make custom Message? TODO
                    return message;
                }
            }
        }
        return null;
    }
}
