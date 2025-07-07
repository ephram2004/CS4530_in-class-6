package sql.metric;

import com.fasterxml.jackson.databind.JsonNode;

import sql.ASQLObj;

public class Metric extends ASQLObj {

    private String metricName;
    private int metricID;
    private int numAccessed;

    public Metric(JsonNode json) {
        super(json);
    }

    @Override
    public String toString() {
        return metricName + ": " + Integer.toString(metricID);
    }
}
