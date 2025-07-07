package sql.metric;

import com.fasterxml.jackson.databind.JsonNode;

import sql.ASQLObj;

public class Metric extends ASQLObj {

    private String metricName;
    private String metricID;
    private int numAccessed;

    public Metric(JsonNode json) {
        super(json);
    }

    public String getMetricName() {
        return this.metricName;
    }

    public String getMetricID() {
        return this.metricID;
    }

    public int getNumAccessed() {
        return this.numAccessed;
    }

    @Override
    public String toString() {
        return metricName + ": " + metricID;
    }
}
