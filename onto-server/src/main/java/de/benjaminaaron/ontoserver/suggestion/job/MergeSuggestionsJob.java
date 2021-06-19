package de.benjaminaaron.ontoserver.suggestion.job;

import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import org.apache.jena.rdf.model.*;

import java.util.*;

public class MergeSuggestionsJob extends Job {

    public MergeSuggestionsJob(Model model) {
        super(model);
    }

    @Override
    public List<Suggestion> execute() {
        startTimer();
        Map<String, UriStats> map = collect();

        tasks.forEach(task -> task.execute(map));
        List<Suggestion> suggestions = new ArrayList<>();
        tasks.forEach(task -> suggestions.addAll(task.getSuggestions()));

        endTimer();
        System.out.println(getJobDurationString());
        return suggestions;
    }

    private Map<String, UriStats> collect() {
        // ResIterator sIter = model.listSubjects();
        // ExtendedIterator<Node> pIter = GraphUtil.listPredicates(model.getGraph(), Node.ANY, Node.ANY);
        // NodeIterator oIter = model.listObjects();
        // Iter.asStream(model.getGraph().find(null, null, null)).count()
        Map<String, UriStats> map = new HashMap<>();

        StmtIterator stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();

            Resource subject = statement.getSubject();
            String sUri = subject.getURI();
            map.putIfAbsent(sUri, new UriStats(sUri, subject.getLocalName()));
            map.get(sUri).usedAsSubject ++;

            Property predicate = statement.getPredicate();
            String pUri = predicate.getURI();
            map.putIfAbsent(pUri, new UriStats(pUri, predicate.getLocalName()));
            map.get(pUri).usedAsPredicate ++;

            RDFNode object = statement.getObject();
            if (object.isResource()) {
                String oUri = object.asResource().getURI();
                map.putIfAbsent(oUri, new UriStats(oUri, object.asResource().getLocalName()));
                map.get(oUri).usedAsObject ++;
            }
        }
        return map;
    }
}
