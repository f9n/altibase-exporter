package com.f9n.altibase.exporter;

import java.util.List;
import java.util.Map;

/** Metric name and help text; key = name without "altibase_" prefix. */
public final class AltibaseMetricDefs {

    private static final String NS = "altibase";

    public static String name(String key) {
        return NS + "_" + key;
    }

    public static List<String> allKeys() {
        return HELP.keySet().stream().sorted().toList();
    }

    private static final Map<String, String> HELP = Map.ofEntries(
            Map.entry("exporter_build_info", "Exporter build identity. Value is 1; label version is the exporter version."),
            Map.entry("version", "Altibase server version. Value is 1; label version is the product version from V$VERSION."),
            Map.entry("exporter_last_scrape_success", "1 if the last scrape succeeded, 0 otherwise."),
            Map.entry("scrape_duration_seconds", "Duration of the last scrape in seconds."),
            Map.entry("instance_working_time_seconds", "Instance working time in seconds (V$INSTANCE WORKING_TIME_SEC)."),
            Map.entry("archive_mode", "Archive mode (0=off, 1=on) from V$ARCHIVE."),
            Map.entry("sessions", "Sessions: total and active (V$SESSION; label status=total|active)."),
            Map.entry("statements", "Statements: total and currently executing (V$STATEMENT; label status=total|active)."),
            Map.entry("memstat_max_total_bytes", "Sum of MAX_TOTAL_SIZE from V$MEMSTAT."),
            Map.entry("memstat_alloc_bytes", "Sum of ALLOC_SIZE from V$MEMSTAT."),
            Map.entry("buffer_pool_hit_ratio", "Buffer pool hit ratio from V$BUFFPOOL_STAT."),
            Map.entry("buffer_pool_victim_fails", "Buffer pool victim failures from V$BUFFPOOL_STAT."),
            Map.entry("logfile_oldest", "Oldest active logfile number (V$ARCHIVE)."),
            Map.entry("logfile_current", "Current logfile number (V$ARCHIVE)."),
            Map.entry("logfile_gap", "Logfile gap: current - oldest (V$ARCHIVE)."),
            Map.entry("lf_prepare_wait_count", "Logfile prepare wait count (V$LFG)."),
            Map.entry("lock_hold_count", "Number of lock holds (V$LOCK_STATEMENT STATE=0)."),
            Map.entry("lock_wait_count", "Number of lock waits (V$LOCK_STATEMENT STATE=1)."),
            Map.entry("long_run_query_count", "Number of long-running queries (execute time > 1s)."),
            Map.entry("utrans_query_count", "Number of uncommitted transaction queries (UTRANS)."),
            Map.entry("fullscan_query_count", "Number of full-scan queries (excluding exporter sessions)."),
            Map.entry("replication_sender_count", "Number of replication senders (V$REPSENDER)."),
            Map.entry("replication_receiver_count", "Number of replication receivers (V$REPRECEIVER)."),
            Map.entry("memory_table_usage_bytes", "Total memory table usage (V$MEMTBL_INFO)."),
            Map.entry("disk_table_usage_bytes", "Total disk table usage (V$DISKTBL_INFO)."),
            Map.entry("memstat_usage_ratio", "Per-name memstat usage ratio (top 10) from V$MEMSTAT."),
            Map.entry("memstat_bytes", "Per-name memory stats (NAME, type=max_total_size|alloc_size)."),
            Map.entry("gc_gap", "GC gap by GC name (V$MEMGC)."),
            Map.entry("tablespace_total_bytes", "Tablespace total size (memory; V$TABLESPACES/V$MEM_TABLESPACES)."),
            Map.entry("tablespace_state", "Tablespace state (1=ONLINE, 0=OFFLINE)."),
            Map.entry("tablespace_usage_ratio", "Tablespace usage ratio (memory)."),
            Map.entry("file_io_reads", "Cumulative physical reads per file (V$FILESTAT PHYRDS)."),
            Map.entry("file_io_writes", "Cumulative physical writes per file (V$FILESTAT PHYWRTS)."),
            Map.entry("file_io_wait_seconds", "Average single-block read wait per file (seconds)."),
            Map.entry("system_event_time_waited_seconds", "System event time waited in seconds (V$SYSTEM_EVENT, non-Idle)."),
            Map.entry("session_event_time_waited_seconds", "Session event time waited in seconds (V$SESSION_EVENT, non-Idle)."),
            Map.entry("memory_table_usage_bytes_per_table", "Memory table usage per table (top 5)."),
            Map.entry("disk_table_usage_bytes_per_table", "Disk table usage per table (top 5)."),
            Map.entry("queue_usage_bytes", "Queue table usage (V$MEMTBL_INFO, TABLE_TYPE=Q)."),
            Map.entry("segment_usage_bytes", "Segment usage by tablespace name (V$SEGMENT)."),
            Map.entry("service_thread_count", "Service thread count by type/state/run_mode (V$SERVICE_THREAD)."),
            Map.entry("sysstat", "System statistics from V$SYSSTAT (name, value)."),
            Map.entry("replication_gap", "Replication gap by replication name (V$REPGAP)."),
            Map.entry("lock_hold_detail", "Top 1 lock hold (session_id, tx_id, table_name, total_time_seconds)."),
            Map.entry("lock_wait_detail", "Top 1 lock wait (session_id, tx_id, wait_for_tx_id, table_name, total_time_seconds)."),
            Map.entry("tx_of_memory_view_scn", "Top 1 tx with memory view SCN (session_id, tx_id, total_time_seconds, execute_time_seconds)."),
            Map.entry("long_run_query_detail", "Top 1 long-running query (session_id, stmt_id, tx_id, times in seconds)."),
            Map.entry("utrans_query_detail", "Top 1 uncommitted transaction query."),
            Map.entry("fullscan_query_detail", "Top 1 full-scan query.")
    );

    public static String help(String key) {
        return HELP.getOrDefault(key, "");
    }

    private AltibaseMetricDefs() {}
}
