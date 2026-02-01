package com.f9n.altibase.exporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/** Loads custom query definitions from YAML: queries: [ { name, help, sql, label_columns? } ]. */
public final class QueriesLoader {

    private static final Logger log = LoggerFactory.getLogger(QueriesLoader.class);

    private QueriesLoader() {}

    @SuppressWarnings("unchecked")
    public static List<CustomQueryCollector.QueryDef> load(Path path) throws IOException {
        String content = Files.readString(path);
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(content);
        if (root == null) return List.of();
        Object queriesObj = root.get("queries");
        if (queriesObj == null || !(queriesObj instanceof List)) return List.of();
        List<Map<String, Object>> list = (List<Map<String, Object>>) queriesObj;
        List<CustomQueryCollector.QueryDef> result = new ArrayList<>();
        for (Map<String, Object> entry : list) {
            String name = getString(entry, "name");
            String help = getString(entry, "help");
            String sql = getString(entry, "sql");
            if (name == null || help == null || sql == null) {
                log.debug("Skipping query entry with missing name/help/sql: {}", entry);
                continue;
            }
            List<String> labelColumns = getStringList(entry, "label_columns");
            result.add(new CustomQueryCollector.QueryDef(name, help, sql, labelColumns));
        }
        log.debug("Loaded {} custom queries from {}", result.size(), path);
        return result;
    }

    private static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isBlank() ? null : s;
    }

    private static List<String> getStringList(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof List) {
            List<String> out = new ArrayList<>();
            for (Object e : (List<?>) v) {
                if (e != null) out.add(e.toString().trim());
            }
            return out.isEmpty() ? null : out;
        }
        return null;
    }
}
