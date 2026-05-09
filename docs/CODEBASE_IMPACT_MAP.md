# CODEBASE_IMPACT_MAP

This map defines expected module boundaries and likely change impact zones before implementation starts.

## Planned Modules

- `app`
- `core-domain`
- `core-gtfs`
- `core-routing`
- `data-local`
- `data-remote`
- `feature-map`
- `feature-search`
- `feature-stop-board`
- `city-adapters`

## Impact Expectations

- Domain model changes will ripple through routing, adapters, and UI features.
- GTFS parser and calendar logic changes are high regression risk.
- Adapter contract changes can break city-specific ingest and realtime logic.

## Mermaid Placeholder

```mermaid
flowchart LR
    app --> feature_map[feature-map]
    app --> feature_search[feature-search]
    app --> feature_stop_board[feature-stop-board]
    feature_map --> core_routing[core-routing]
    feature_search --> core_domain[core-domain]
    feature_stop_board --> data_local[data-local]
    data_local --> core_gtfs[core-gtfs]
    data_remote[data-remote] --> city_adapters[city-adapters]
    city_adapters --> core_domain
```
