# Altibase mixin

Grafana dashboards (and optionally alerts/rules) for [altibase-exporter](https://github.com/f9n/altibase-exporter), in [Prometheus mixin](https://github.com/monitoring-mixins/docs) style. Layout inspired by [node_exporter node-mixin](https://github.com/prometheus/node_exporter/tree/master/docs/node-mixin).

**Quick start (from repo root):**

```bash
make -C docs/altibase-mixin all
# Import in Grafana: docs/altibase-mixin/generated/altibase.json (or from Grafana.com once published)
```

## Requirements

- [jsonnet](https://jsonnet.org/) (e.g. `brew install jsonnet`)
- [jq](https://jqlang.github.io/jq/) (for post-processing the generated JSON)

## Layout

```
config.libsonnet    # Dashboard title, uid, tags, refresh (override when importing mixin)
mixin.libsonnet     # Exposes grafanaDashboards (and later alerts/rules)
dashboards.jsonnet  # Entry for `make generated` (-m multi-file output)
dashboards/         # Dashboard definitions
lib/                # Panel helpers (stat, timeseries)
generated/          # Generated JSON (committed; altibase.json — import in Grafana or from Grafana.com)
```

## Targets

- **`make`** or **`make all`** — fmt, generated, lint
- **`make fmt`** — Format all `.jsonnet` / `.libsonnet` with `jsonnetfmt`
- **`make generated`** — Generate `generated/altibase.json`
- **`make lint`** — Ensure sources are formatted and dashboards build
- **`make clean`** — Remove `generated/`

## Using the mixin from this repo

From repo root:

```bash
make -C docs/altibase-mixin generated
# Import in Grafana: docs/altibase-mixin/generated/altibase.json
```

## Using the mixin from another repo

Copy this directory (or add as submodule) and override config:

```jsonnet
// my-dashboards.jsonnet
local altibase = import 'altibase-mixin/mixin.libsonnet';
local config = altibase.config + {
  dashboardTitle: 'My Altibase',
  dashboardUid: 'my-altibase',
};
// Then use altibase.grafanaDashboards.altibase with config overrides if the mixin supports it.
```

Currently the mixin uses a single global `config.libsonnet`; parameterized config can be added later.

## CI

On push/PR under `docs/altibase-mixin/**`, [`.github/workflows/grafana-dashboard.yml`](../../.github/workflows/grafana-dashboard.yml) regenerates the dashboard and on `main` commits `docs/altibase-mixin/generated/altibase.json`; optionally deploys to Grafana if `GRAFANA_URL` / `GRAFANA_API_KEY` secrets are set. Run `make lint` locally (requires `jsonnetfmt`) to check format. After publishing to [Grafana.com](https://grafana.com/grafana/dashboards/), link the dashboard page (e.g. `https://grafana.com/grafana/dashboards/XXXX-altibase-exporter/`) in the main README.

## Grafana.com upload

The generated JSON includes `__inputs` and `__requires` sections (the "Export for sharing externally" format). This is required by Grafana.com — without it, the uploader shows "Old dashboard JSON format". Panel helpers in `lib/panels.libsonnet` use `${DS_PROMETHEUS}` as datasource uid, matching the `__inputs` declaration.
