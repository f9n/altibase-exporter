package com.f9n.altibase.exporter;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueriesLoaderTest {

    @Test
    void load_emptyFile_returnsEmptyList() throws IOException {
        Path tmp = Files.createTempFile("queries", ".yaml");
        try {
            Files.writeString(tmp, "");
            List<CustomQueryCollector.QueryDef> result = QueriesLoader.load(tmp);
            assertTrue(result.isEmpty());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void load_noQueriesKey_returnsEmptyList() throws IOException {
        Path tmp = Files.createTempFile("queries", ".yaml");
        try {
            Files.writeString(tmp, "other: value\n");
            List<CustomQueryCollector.QueryDef> result = QueriesLoader.load(tmp);
            assertTrue(result.isEmpty());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void load_validYaml_returnsQueryDefs() throws IOException {
        Path tmp = Files.createTempFile("queries", ".yaml");
        try {
            String yaml = """
                queries:
                  - name: test_metric
                    help: "Test help"
                    sql: "SELECT 1 AS value"
                """;
            Files.writeString(tmp, yaml);
            List<CustomQueryCollector.QueryDef> result = QueriesLoader.load(tmp);
            assertEquals(1, result.size());
            assertEquals("test_metric", result.get(0).name());
            assertEquals("Test help", result.get(0).help());
            assertEquals("SELECT 1 AS value", result.get(0).sql());
            assertEquals(null, result.get(0).labelColumns());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void load_withLabelColumns() throws IOException {
        Path tmp = Files.createTempFile("queries", ".yaml");
        try {
            String yaml = """
                queries:
                  - name: rep_items
                    help: "Repl items"
                    sql: "SELECT rep_name, COUNT(*) AS value FROM T GROUP BY rep_name"
                    label_columns: [rep_name]
                """;
            Files.writeString(tmp, yaml);
            List<CustomQueryCollector.QueryDef> result = QueriesLoader.load(tmp);
            assertEquals(1, result.size());
            assertEquals(List.of("rep_name"), result.get(0).labelColumns());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void load_skipsEntryWithMissingNameHelpSql() throws IOException {
        Path tmp = Files.createTempFile("queries", ".yaml");
        try {
            String yaml = """
                queries:
                  - name: ok
                    help: "Help"
                    sql: "SELECT 1"
                  - name: ""
                    help: "H"
                    sql: "SELECT 2"
                  - other: junk
                """;
            Files.writeString(tmp, yaml);
            List<CustomQueryCollector.QueryDef> result = QueriesLoader.load(tmp);
            assertEquals(1, result.size());
            assertEquals("ok", result.get(0).name());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
