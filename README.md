# Altibase Prometheus Exporter

A Prometheus exporter for [Altibase](https://www.altibase.com/).

For local build, run, and releases, see [DEVELOPMENT.md](DEVELOPMENT.md).

## Where to run

The exporter needs only JDBC access to Altibase (no ODBC or Altibase install on the host).

| Where | When to use | Example |
|-------|-------------|--------|
| **Host (VM / bare metal)** | Same machine as Altibase, or any server that can reach Altibase. Java 25 required. | Download JAR from [Releases](https://github.com/f9n/altibase-exporter/releases), set `ALTIBASE_*` env, run `java -jar altibase-exporter.jar`. Metrics at `http://<host>:9399/metrics`. |
| **Container (Docker)** | Isolated run without installing Java; same host or another host. | Pull image from GHCR (`ghcr.io/f9n/altibase-exporter`), then `docker run` with env vars. Details below. |
| **Kubernetes** | Exporter in a cluster (e.g. separate from Altibase); ConfigMap + Secret for config. | Edit `examples/k8s/configmap.yaml`, create secret, `kubectl apply -f examples/k8s/`. Details below. |

## Running

**Environment variables:**

| Variable | Description | Default |
|----------|-------------|---------|
| `ALTIBASE_SERVER` | Altibase server host | 127.0.0.1 |
| `ALTIBASE_PORT` | Altibase port | 20300 |
| `ALTIBASE_USER` | Database user | — |
| `ALTIBASE_PASSWORD` | Password | — |
| `ALTIBASE_DATABASE` | Database name | mydb |
| `WEB_LISTEN_PORT` | Exporter HTTP port | 9399 |
| `ALTIBASE_QUERIES_FILE` | Path to custom queries YAML (optional) | — |
| `ALTIBASE_DISABLED_METRICS` | Comma-separated list of built-in metric keys to disable (e.g. `sysstat`, `replication_gap`) | — |
| `LOG_LEVEL` | Log level: `DEBUG`, `INFO`, `WARN`, `ERROR`. Logs are JSON (structured) to stdout. | INFO |

Run: set env or use command-line flags (see [DEVELOPMENT.md](DEVELOPMENT.md)). Metrics at `http://localhost:9399/metrics`.

## Metrics

The exporter collects **built-in** metrics from Altibase V$ system views. It also exposes **JVM and process** metrics (`jvm_*`, `process_*`) from the Prometheus Java client instrumentation (memory, threads, GC, CPU, etc.). All built-in metrics are enabled by default. To disable specific metrics, set `ALTIBASE_DISABLED_METRICS` to a comma-separated list of **metric keys** (metric name without the `altibase_` prefix). Example: `ALTIBASE_DISABLED_METRICS=sysstat,replication_gap`. **Every** built-in metric can be disabled except the exporter identity/health metrics (`altibase_exporter_build_info`, `altibase_exporter_last_scrape_success`, `altibase_scrape_duration_seconds`, `altibase_version_info`). For extra metrics use **custom queries** (queries.yaml) below.

| Metric | Description |
|--------|-------------|
| `altibase_exporter_build_info` | Exporter build identity (Info). Value 1; label `version` = exporter version. |
| `altibase_exporter_last_scrape_success` | 1 if the last scrape succeeded, 0 otherwise. |
| `altibase_scrape_duration_seconds` | Duration of the last scrape in seconds. |
| `altibase_instance_working_time_seconds` | Instance working time (V$INSTANCE). |
| `altibase_version_info` | Altibase server version (Info). Value 1; label `version` = product version from V$VERSION. |
| `altibase_archive_mode` | Archive mode 0/1 (V$ARCHIVE). |
| `altibase_sessions` | Session count. Label `status`: `total` = all, `active` = active (ACTIVE_FLAG=1). |
| `altibase_statements` | Statement count. Label `status`: `total` = all, `active` = currently executing (EXECUTE_FLAG=1). |
| `altibase_memstat_max_total_bytes` | Sum of MAX_TOTAL_SIZE from V$MEMSTAT. |
| `altibase_memstat_alloc_bytes` | Sum of ALLOC_SIZE from V$MEMSTAT. |
| `altibase_memstat_usage_ratio` | Per-name memstat usage ratio, top 10 (label `name`). |
| `altibase_memstat_bytes` | Per-name memstat max_total_size and alloc_size (label `name`, `type`). |
| `altibase_buffer_pool_hit_ratio` | Buffer pool hit ratio (V$BUFFPOOL_STAT). |
| `altibase_buffer_pool_victim_fails` | Buffer pool victim failures (V$BUFFPOOL_STAT). |
| `altibase_logfile_oldest` | Oldest active logfile number (V$ARCHIVE). |
| `altibase_logfile_current` | Current logfile number (V$ARCHIVE). |
| `altibase_logfile_gap` | Logfile gap: current − oldest (V$ARCHIVE). |
| `altibase_lf_prepare_wait_count` | Logfile prepare wait count (V$LFG). |
| `altibase_lock_hold_count` | Number of lock holds (V$LOCK_STATEMENT STATE=0). |
| `altibase_lock_wait_count` | Number of lock waits (V$LOCK_STATEMENT STATE=1). |
| `altibase_long_run_query_count` | Number of long-running queries (execute time &gt; 1s). |
| `altibase_utrans_query_count` | Number of uncommitted transaction queries (UTRANS). |
| `altibase_fullscan_query_count` | Number of full-scan queries (excluding exporter sessions). |
| `altibase_replication_sender_count` | Number of replication senders (V$REPSENDER). |
| `altibase_replication_receiver_count` | Number of replication receivers (V$REPRECEIVER). |
| `altibase_replication_gap` | Replication gap by name (V$REPGAP; label `replication`). |
| `altibase_memory_table_usage_bytes` | Total memory table usage (V$MEMTBL_INFO). |
| `altibase_disk_table_usage_bytes` | Total disk table usage (V$DISKTBL_INFO). |
| `altibase_memory_table_usage_bytes_per_table` | Memory table usage per table, top 5 (label `table_name`). |
| `altibase_disk_table_usage_bytes_per_table` | Disk table usage per table, top 5 (label `table_name`). |
| `altibase_service_thread_count` | Service thread count by type/state/run_mode (V$SERVICE_THREAD; label `kind`, `value`). |
| `altibase_sysstat` | V$SYSSTAT values (label `name`). |
| `altibase_gc_gap` | GC gap by GC name (V$MEMGC; label `gc_name`). |
| `altibase_tablespace_total_bytes` | Tablespace total size, memory (label `tbs_name`). |
| `altibase_tablespace_usage_ratio` | Tablespace usage ratio, memory (label `tbs_name`). |
| `altibase_tablespace_state` | Tablespace state 1=ONLINE, 0=OFFLINE (label `tbs_name`, `state`). |
| `altibase_file_io_reads` | Cumulative physical reads per file (V$FILESTAT; label `file_name`). |
| `altibase_file_io_writes` | Cumulative physical writes per file (V$FILESTAT; label `file_name`). |
| `altibase_file_io_wait_seconds` | Average single-block read wait per file, seconds (label `file_name`). |
| `altibase_system_event_time_waited_seconds` | System event time waited in seconds, non-Idle (label `event`). |
| `altibase_session_event_time_waited_seconds` | Session event time waited in seconds, non-Idle (label `event`). |
| `altibase_queue_usage_bytes` | Queue table usage (label `table_name`). |
| `altibase_segment_usage_bytes` | Segment usage by tablespace (label `name`). |
| `altibase_lock_hold_detail` | Top 1 lock hold (labels: session_id, tx_id, table_name, total_time_seconds, query, …). |
| `altibase_lock_wait_detail` | Top 1 lock wait (labels: session_id, tx_id, wait_for_tx_id, table_name, total_time_seconds, query, …). |
| `altibase_tx_of_memory_view_scn` | Top 1 tx with memory view SCN (labels: session_id, tx_id, total_time_seconds, execute_time_seconds, query). |
| `altibase_long_run_query_detail` | Top 1 long-running query (labels: session_id, stmt_id, tx_id, prepare_time_seconds, …, total_time_seconds, query). |
| `altibase_utrans_query_detail` | Top 1 uncommitted transaction query (labels: session_id, client_ip, utrans_time_seconds, execute_time_seconds, total_time_seconds, query, …). |
| `altibase_fullscan_query_detail` | Top 1 full-scan query (labels: session_id, client_ip, prepare_time_seconds, …, total_time_seconds, query). |

## Custom queries (SQL exporter style)

To run your own SQL and expose results as Prometheus gauges, use a **queries file** (YAML). Set the path via env `ALTIBASE_QUERIES_FILE` or flag `-altibase.queries-file=<path>` (e.g. `ALTIBASE_QUERIES_FILE=examples/queries.yaml`).

**Format and examples:** see `examples/queries.yaml`. Each entry has **name** (metric name), **help** (description), **sql** (must return a numeric `value` column), and optional **label_columns** (column names that become gauge labels). Result rows become gauge data points at scrape time. Custom metric **names must not** match any built-in `altibase_*` metric name (same registry, no duplicates).

If the queries file is missing or the path is empty, only built-in metrics are collected.

## Prometheus configuration

Add a scrape config so Prometheus collects metrics from the exporter:

```yaml
scrape_configs:
  - job_name: 'altibase'
    static_configs:
      - targets: ['<exporter-host>:9399']
```

Replace `<exporter-host>` with the host or IP where the exporter is running (e.g. `localhost:9399` for local, or the k8s service name in-cluster).

## Docker

**1. Pull the image** from [GitHub Container Registry](https://github.com/f9n/altibase-exporter/pkgs/container/altibase-exporter):

```bash
docker pull ghcr.io/f9n/altibase-exporter:latest
```

Or use a specific release tag (e.g. `v1.0.0`): `docker pull ghcr.io/f9n/altibase-exporter:v1.0.0`.

**2. Run the container** (set your Altibase connection with env vars):

```bash
docker run -d --name altibase-exporter -p 9399:9399 \
  -e ALTIBASE_SERVER=<host> \
  -e ALTIBASE_PORT=<port> \
  -e ALTIBASE_USER=<user> \
  -e ALTIBASE_PASSWORD=<password> \
  ghcr.io/f9n/altibase-exporter:latest
```

**3. Check metrics:**

```bash
curl http://localhost:9399/metrics
```

Or open `http://localhost:9399` / `http://localhost:9399/metrics` in a browser.

**4. Stop and remove:**

```bash
docker stop altibase-exporter
docker rm altibase-exporter
```

**Optional — build the image locally** (from project root): `docker build -t altibase-exporter .` then use `altibase-exporter` as the image name in `docker run`.

## Kubernetes

Example manifests are in the `examples/k8s/` directory. The exporter runs as a Deployment; connection settings come from a ConfigMap and a Secret. The deployment uses `ghcr.io/f9n/altibase-exporter:latest` by default (or use a release tag, e.g. `:v1.0.0`).

**1. Create the Secret** (use real credentials; do not commit secrets):

```bash
kubectl create secret generic altibase-exporter-secret \
  --from-literal=ALTIBASE_USER=<user> \
  --from-literal=ALTIBASE_PASSWORD=<password>
```

**2. Edit and apply manifests:**

- `examples/k8s/configmap.yaml` — set `ALTIBASE_SERVER`, `ALTIBASE_PORT`, `ALTIBASE_DATABASE` (and optionally `WEB_LISTEN_PORT`).
- `examples/k8s/deployment.yaml` — uses `ghcr.io/f9n/altibase-exporter:latest` by default; change the image/tag if needed.

```bash
kubectl apply -f examples/k8s/configmap.yaml
kubectl apply -f examples/k8s/secret.yaml    # or use the create secret command above
kubectl apply -f examples/k8s/deployment.yaml
kubectl apply -f examples/k8s/service.yaml
```

**3. Prometheus scraping** — the Service has annotations (`prometheus.io/scrape`, `prometheus.io/port`, `prometheus.io/path`) so Prometheus can discover and scrape it when using Kubernetes endpoints discovery with relabel configs that honour these annotations (e.g. many Helm-based Prometheus setups).

Pods must be able to reach the Altibase server (e.g. same VPC, or exposed host/port).

**4. Custom queries (optional)** — `examples/k8s/configmap.yaml` includes an optional `queries.yaml` key. To use it, deploy with `examples/k8s/deployment-with-queries.yaml` instead of `examples/k8s/deployment.yaml` (same ConfigMap; that deployment mounts the key as a file and sets `ALTIBASE_QUERIES_FILE`).

## Grafana

A minimal dashboard is in `examples/grafana/dashboard.json`. In Grafana: **Dashboards → New → Import → Upload JSON file**, choose `examples/grafana/dashboard.json`, then select your Prometheus datasource. The dashboard includes panels for **Up**, **Scrape duration**, **Sessions**, **Statements**, **Buffer pool hit ratio**, and **Locks**. You can add more panels using the metric names listed in the Metrics section.
