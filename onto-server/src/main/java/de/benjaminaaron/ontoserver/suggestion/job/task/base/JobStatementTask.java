package de.benjaminaaron.ontoserver.suggestion.job.task.base;

import de.benjaminaaron.ontoserver.model.Utils;
import de.benjaminaaron.ontoserver.suggestion.Suggestion;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.List;

import static de.benjaminaaron.ontoserver.model.Utils.ResourceType.*;

public abstract class JobStatementTask extends JobTask {

    private Statement statement;

    @Override
    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    @Override
    public List<Suggestion> execute() {
        check(statement.getSubject(), SUBJECT);
        check(statement.getPredicate(), PREDICATE);
        if (statement.getResource().isResource()) {
            check(statement.getObject().asResource(), OBJECT);
        }
        return suggestions;
    }

    protected abstract void check(Resource resource, Utils.ResourceType resourceType);
}
