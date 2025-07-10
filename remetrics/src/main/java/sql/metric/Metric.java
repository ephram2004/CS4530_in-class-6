package sql.metric;

import com.fasterxml.jackson.databind.JsonNode;

import sqlobjs.ASQLObj;

public class Metric extends ASQLObj {

    private String metricName;
    private String metricId;
    private int numAccessed;

    public Metric(JsonNode json) {
        super(json);
    }

    public String getMetricName() {
        return this.metricName;
    }

    public String getMetricId() {
        return this.metricId;
    }

    public int getNumAccessed() {
        return this.numAccessed;
    }

    @Override
    public String toString() {
        return metricName + ": " + metricId;
    }
}
