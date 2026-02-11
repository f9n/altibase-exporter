// Altibase mixin: Grafana dashboards (and optionally alerts/rules) for altibase-exporter.
// Import with: local altibase = import 'mixin.libsonnet';
local config = import 'config.libsonnet';

{
  config:: config,
  grafanaDashboards: {
    altibase: (import 'dashboards/altibase.jsonnet'),
  },
}
