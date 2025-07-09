package sql.metric;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import sqlobjs.ADAO;

public class MetricsDAO extends ADAO {

    public void addOrIncrementNumAccessed(Metric metric) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO public.metrics (metric_name, metric_id, num_accessed)\n"
                + //
                "VALUES (?, ?, 1)\n"
                + //
                "ON CONFLICT (metric_name, metric_id)\n"
                + //
                "DO UPDATE SET num_accessed = metrics.num_accessed + 1;")) {
            stmt.setString(1, metric.getMetricName());
            stmt.setString(2, metric.getMetricID());
            stmt.executeUpdate();
        }
    }

    public Optional<Metric> findMetricByID(String metricID, String metricName) throws SQLException {
        String sql = "SELECT * FROM metrics WHERE metric_id = ? AND metric_name = ?";
        Metric metric = null;
        try (
                Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, metricID);
            stmt.setString(2, metricName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    metric = this.mapResultSetToMetric(rs);
                }
            }
        }
        return Optional.ofNullable(metric);
    }

    private Metric mapResultSetToMetric(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = meta.getColumnLabel(i);
                Object value = rs.getObject(i);
                row.put(columnName, value);
            }
            rows.add(row);
        }

        ObjectMapper mapper = new ObjectMapper();
        return new Metric(mapper.valueToTree(rows));
    }
}
