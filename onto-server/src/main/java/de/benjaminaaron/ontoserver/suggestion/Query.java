package de.benjaminaaron.ontoserver.suggestion;

import lombok.Data;

@Data
public class Query {
    private String instantiatedFromTemplate;
    private String queryName;
    private QueryType type;
    private String query;

    @Override
    public String toString() {
        return "queryName: " + queryName + ", type: " + type + ", query: " + query;
    }

    public enum QueryType {
        TEMPLATE, PERIODIC, UNKNOWN;

        public static QueryType parse(String queryType) {
            if (queryType.equalsIgnoreCase("templatePeriodicQuery")) {
                return TEMPLATE;
            }
            if (queryType.equalsIgnoreCase("periodicQuery")) {
                return PERIODIC;
            }
            return UNKNOWN;
        }
    }
}
