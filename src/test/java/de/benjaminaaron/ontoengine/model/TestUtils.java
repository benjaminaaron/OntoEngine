package de.benjaminaaron.ontoengine.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class TestUtils {

    static List<Statement> statementIteratorToList(StmtIterator iter) {
       List<Statement> list = new ArrayList<>();
       while (iter.hasNext()) {
        list.add(iter.nextStatement());
       }
       return list;
    }
}
