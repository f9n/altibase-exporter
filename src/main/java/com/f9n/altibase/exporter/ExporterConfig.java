package com.f9n.altibase.exporter;

import java.util.Set;

/** Exporter configuration from env and command-line args. */
public record ExporterConfig(
        String server,
        int port,
        String user,
        String password,
        String database,
        int listenPort,
        String queriesFile,
        int connectTimeoutSeconds,
        Set<String> disabledMetrics,
        String exporterVersion
) {
    public ExporterConfig {
        disabledMetrics = disabledMetrics != null ? Set.copyOf(disabledMetrics) : Set.of();
    }

    public Set<String> disabledMetrics() {
        return Set.copyOf(disabledMetrics);
    }

    public String jdbcUrl() {
        return "jdbc:Altibase://" + server + ":" + port + "/" + database;
    }
}
