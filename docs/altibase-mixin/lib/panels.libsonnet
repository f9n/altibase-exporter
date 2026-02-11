// Panel helpers for Altibase Grafana dashboard.
// Usage: local panels = import 'lib/panels.libsonnet';

// DS_PROMETHEUS matches __inputs[].name in the dashboard (required for Grafana.com upload).
local ds = { type: 'prometheus', uid: '${DS_PROMETHEUS}' };

local legendTable = {
  legend: {
    calcs: ['max', 'lastNotNull'],
    displayMode: 'table',
    placement: 'right',
    showLegend: true,
    sortBy: 'Last',
    sortDesc: false,
  },
};

// Build a Prometheus target: { datasource, expr, legendFormat?, refId }
local target(expr, legendFormat, refId) = {
  datasource: ds,
  expr: expr,
  refId: refId,
} + (if legendFormat != null then { legendFormat: legendFormat } else {});

// Stat (gauge) panel. pos = { h, w, x, y }. unit default "short". extra = fieldConfig/options overrides.
local statPanel(id, title, expr, pos, unit='short', extra={}) = {
  datasource: ds,
  fieldConfig: {
    defaults: {
      color: { mode: 'palette-classic' },
      unit: unit,
      mappings: [],
      thresholds: null,
    } + std.get(extra, 'fieldConfig', {}),
    overrides: [],
  },
  gridPos: pos,
  id: id,
  options: {
    colorMode: 'value',
    graphMode: 'none',
    justifyMode: 'auto',
    orientation: 'auto',
    reduceOptions: { calcs: ['lastNotNull'], fields: '', values: false },
    textMode: 'auto',
    legend: { displayMode: 'list', placement: 'bottom', showLegend: true },
  } + std.get(extra, 'options', {}),
  targets: [target(expr, null, 'A')],
  title: title,
  type: 'stat',
} + std.get(extra, 'panel', {});

// Timeseries panel. targets = [ { expr, legendFormat } ] or single { expr, legendFormat }.
// pos = { h, w, x, y }. unit default "short".
local timeSeriesPanel(id, title, targetsIn, pos, unit='short', extra={}) = {
  datasource: ds,
  fieldConfig: {
    defaults: {
      color: { mode: 'palette-classic' },
      unit: unit,
    } + std.get(extra, 'fieldConfig', {}),
    overrides: [],
  },
  gridPos: pos,
  id: id,
  options: legendTable + std.get(extra, 'options', {}),
  targets: std.mapWithIndex(
    function(i, t)
      target(
        t.expr,
        if std.objectHas(t, 'legendFormat') then t.legendFormat else null,
        std.char(65 + i)
      ),
    if std.type(targetsIn) == 'array' then targetsIn else [targetsIn]
  ),
  title: title,
  type: 'timeseries',
} + std.get(extra, 'panel', {});

{
  ds: ds,
  target: target,
  statPanel: statPanel,
  timeSeriesPanel: timeSeriesPanel,
  legendTable: legendTable,
}
