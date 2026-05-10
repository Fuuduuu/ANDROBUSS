# ANDROBUSS

ANDROBUSS is a standalone Android bus app for practical public transit use in Estonian cities.

## Direction

- Standalone Android app (not an AnniVibe UI clone).
- Modular, city-agnostic transit core.
- Destination-first UX.
- Offline-first behavior.
- Static GTFS as canonical base, realtime optional later.

## Current Status

The project is beyond planning-only stage.

Implemented and tested core modules:
- `core-domain`: canonical stop/pattern/service models and `ServiceCalendarResolver`.
- `core-gtfs`: minimal GTFS fixture parser, CSV reader, and domain mapper.
- `core-routing`: minimal direct-route search core.
- `feature-search`: destination/origin candidates, stop resolution, enrichment/orchestration, and route-query preparation.

Not implemented yet:
- Room cache/schema.
- City adapter runtime implementation.
- Production feed downloader/sync.
- Compose feature UI flows.
- Transfer routing and realtime logic.

## Process

Docs-first, pass-based, sniper-prompt workflow.

## Next Pass

`PASS 21 — FEED_DOMAIN_SNAPSHOT_AND_ROUTE_PATTERN_PROVIDER_SPEC`
