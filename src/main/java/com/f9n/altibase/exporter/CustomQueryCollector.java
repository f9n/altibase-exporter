package com.f9n.altibase.exporter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** MultiCollector that runs custom SQL from config; each row → one gauge data point (labels + value). */
public final class CustomQueryCollector implements MultiCollector {

    /** Mandatory prefix for all custom query metric names; avoids clash with built-in altibase_* metrics. */
    public static final String CUSTOM_METRIC_PREFIX = "altibase_custom_";

    private static final Logger log = LoggerFactory.getLogger(CustomQueryCollector.class);
    private final Connection conn;
    private final List<QueryDef> queries;
    private final List<String> metricNames;

    public CustomQueryCollector(Connection conn, List<QueryDef> queries) {
        this.conn = conn;
        this.queries = List.copyOf(queries);
        this.metricNames = queries.stream().map(q -> customMetricName(q.name())).toList();
    }

    private static String customMetricName(String name) {
        if (name == null || name.isEmpty()) return CUSTOM_METRIC_PREFIX + "unnamed";
        return name.startsWith(CUSTOM_METRIC_PREFIX) ? name : CUSTOM_METRIC_PREFIX + name;
    }

    @Override
    public MetricSnapshots collect() {
        List<MetricSnapshot> snapshots = new ArrayList<>();
        for (QueryDef q : queries) {
            try {
                runQuery(q, snapshots);
            } catch (Exception e) {
                log.warn("Custom query failed: name={} error={}", customMetricName(q.name()), e.getMessage());
            }
        }
        return new MetricSnapshots(snapshots);
    }

    @Override
    public List<String> getPrometheusNames() {
        return List.copyOf(metricNames);
    }

    private void runQuery(QueryDef q, List<MetricSnapshot> out) throws SQLException {
        List<GaugeSnapshot.GaugeDataPointSnapshot> points = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(q.sql())) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            List<String> labelCols = q.labelColumns() != null ? q.labelColumns() : inferLabelColumns(meta, colCount);
            int valueColIndex = findValueColumn(meta, colCount, labelCols);

            while (rs.next()) {
                List<String> pairs = new ArrayList<>();
                for (String labelName : labelCols) {
                    String val = getString(rs, meta, labelName);
                    pairs.add(sanitizeLabelName(labelName));
                    pairs.add(Objects.requireNonNullElse(val, ""));
                }
                Labels labels = pairs.isEmpty() ? Labels.EMPTY : Labels.of(pairs.toArray(new String[0]));
                double value = getNumeric(rs, valueColIndex);
                points.add(new GaugeSnapshot.GaugeDataPointSnapshot(value, labels, null));
            }
        }
        if (!points.isEmpty()) {
            GaugeSnapshot.Builder b = GaugeSnapshot.builder().name(customMetricName(q.name())).help(q.help());
            for (var p : points) b.dataPoint(p);
            out.add(b.build());
        }
    }

    private static List<String> inferLabelColumns(ResultSetMetaData meta, int colCount) throws SQLException {
        List<String> labelCols = new ArrayList<>();
        for (int i = 1; i < colCount; i++) {
            labelCols.add(meta.getColumnLabel(i));
        }
        return labelCols;
    }

    private static int findValueColumn(ResultSetMetaData meta, int colCount, List<String> labelCols) throws SQLException {
        for (int i = 1; i <= colCount; i++) {
            String label = meta.getColumnLabel(i);
            if ("value".equalsIgnoreCase(label)) return i;
            boolean isLabel = false;
            for (String lc : labelCols) {
                if (lc.equalsIgnoreCase(label)) { isLabel = true; break; }
            }
            if (!isLabel) return i;
        }
        return colCount;
    }

    private static String getString(ResultSet rs, ResultSetMetaData meta, String columnLabel) throws SQLException {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (columnLabel.equalsIgnoreCase(meta.getColumnLabel(i))) {
                Object o = rs.getObject(i);
                return o != null ? o.toString() : "";
            }
        }
        return "";
    }

    private static double getNumeric(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getDouble(columnIndex);
    }

    private static String sanitizeLabelName(String name) {
        return name == null ? "" : name.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase(Locale.ROOT);
    }

    public record QueryDef(String name, String help, String sql, List<String> labelColumns) {
        public QueryDef {
            labelColumns = labelColumns == null ? null : List.copyOf(labelColumns);
        }

        @Override
        public List<String> labelColumns() {
            return labelColumns == null ? null : List.copyOf(labelColumns);
        }
    }
}
