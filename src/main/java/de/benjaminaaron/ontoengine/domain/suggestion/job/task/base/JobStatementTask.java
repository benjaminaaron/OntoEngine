package de.benjaminaaron.ontoengine.domain.suggestion.job.task.base;

import de.benjaminaaron.ontoengine.domain.Utils;
import de.benjaminaaron.ontoengine.domain.suggestion.Suggestion;
import org.apache.jena.rdf.model.Statement;

import java.util.List;
import java.util.Objects;

import static de.benjaminaaron.ontoengine.domain.Utils.ResourceType.*;

public abstract class JobStatementTask extends JobTask {

    private Statement statement;

    @Override
    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    @Override
    public List<Suggestion> execute() {
        if (Objects.nonNull(statement)) {
            check(statement, SUBJECT);
            check(statement, PREDICATE);
            if (statement.getObject().isResource()) {
                check(statement, OBJECT);
            }
        }
        return suggestions;
    }

    protected abstract void check(Statement statement, Utils.ResourceType resourceType);
}
