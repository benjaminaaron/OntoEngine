package de.benjaminaaron.ontoserver.model.suggestion.job;

import de.benjaminaaron.ontoserver.model.suggestion.Suggestion;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.*;

public class PoolUniqueUrisAndTheirWordsJob extends Job {

    public PoolUniqueUrisAndTheirWordsJob(Model model) {
        super(model);
    }

    @Override
    public List<Suggestion> execute() {
        startTimer();
        Map<String, String> map = collect();

        // extract to CaseSensitivityTask?
        Map<String, Set<String>> buckets = new HashMap<>();
        map.forEach((uri, word) -> {
            String key = word.toLowerCase();
            buckets.putIfAbsent(key, new HashSet<>());
            buckets.get(key).add(uri);
        });
        Map<String, Set<String>> multiBuckets = new HashMap<>();
        buckets.forEach((key, uris) -> {
            if (uris.size() > 1) {
                multiBuckets.put(key, uris);
            }
        });
        System.out.println(multiBuckets);
        // TODO

        endTimer();
        System.out.println(getJobDurationString());
        return null;
    }

    private Map<String, String> collect() {
        Map<String, String> map = new HashMap<>();
        ResIterator sIter = model.listSubjects();
        while (sIter.hasNext()) {
            Resource resource = sIter.next();
            map.put(resource.getURI(), resource.getLocalName());
        }
        ExtendedIterator<Node> pIter = GraphUtil.listPredicates(model.getGraph(), Node.ANY, Node.ANY);
        while (pIter.hasNext()) {
            Node node = pIter.next();
            map.put(node.toString(), node.getLocalName());
        }
        NodeIterator oIter = model.listObjects();
        while (oIter.hasNext()) {
            RDFNode rdfNode = oIter.next();
            if (rdfNode.isResource()) {
                Resource resource = rdfNode.asResource();
                map.put(resource.getURI(), resource.getLocalName());
            }
        }
        return map;
    }
}
