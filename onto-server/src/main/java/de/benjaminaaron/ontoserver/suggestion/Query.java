package de.benjaminaaron.ontoserver.suggestion;

import lombok.Data;

@Data
public class Query {
    private String instantiatedFromTemplate = ""; // the template's name
    private final String queryName;
    private final QueryType type;
    private final String query;

    public Query(QueryType type, String queryName, String query) {
        this.type = type;
        this.queryName = queryName;
        this.query = query;
    }

    public Query(QueryType type, String queryName, String query, String instantiatedFromTemplate) {
        this(type, queryName, query);
        this.instantiatedFromTemplate = instantiatedFromTemplate;
    }

    @Override
    public String toString() {
        return "type: " + type + ", queryName: " + queryName + ", instantiatedFromTemplate: " + instantiatedFromTemplate + ", query: " + query;
    }

    public enum QueryType {
        TEMPLATE, PERIODIC
    }
}
