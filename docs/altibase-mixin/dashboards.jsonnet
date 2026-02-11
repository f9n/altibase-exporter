// Entry point for jsonnet -m generated dashboards.jsonnet
// Renders all Grafana dashboards to generated/<name>.json
local mixin = import 'mixin.libsonnet';

{
  'altibase.json': mixin.grafanaDashboards.altibase,
}
