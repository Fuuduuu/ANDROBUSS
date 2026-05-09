# CURRENT_STATE

## Repository Baseline

- Expected repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`.
- Expected branch: `main`.
- Last accepted runtime pass HEAD: `d8d91b0` (`PASS 08`).
- Working tree should be clean before starting a new pass.

## Accepted Passes

- PASS 00: docs bootstrap.
- PASS 01B: architecture review fixes.
- PASS 02: Android/Gradle skeleton.
- PASS 03: GTFS source discovery.
- PASS 04: GTFS fixture and city/feed mapping strategy.
- PASS 05: core-domain stop/pattern models.
- PASS 05B: namespace and guardrail cleanup.
- PASS 06: `ServiceCalendarResolver` spec + tests.
- PASS 07: minimal GTFS fixture parser.
- PASS 08: direct-route search core.

## Implemented Core Stack

- `core-domain`:
  - Transit IDs, `GeoPoint`, `StopGroup`, `StopPoint`, `RouteLine`, `PatternStop`, `RoutePattern`, `Trip`, `DataConfidence`.
  - `ServiceCalendar`, `ServiceCalendarException`, `ServiceCalendarResolver`.
  - Executable invariants and calendar tests.
- `core-gtfs`:
  - `CsvTableReader`, `GtfsFeedParser`, `GtfsDomainMapper`, GTFS raw models and parse exception.
  - Synthetic tiny fixture: `core-gtfs/src/test/resources/gtfs/rakvere-smoke/`.
  - Executable CSV/parser/mapper tests.
- `core-routing`:
  - Direct-route models and `DirectRouteSearch`.
  - Executable direct-route tests, including duplicate-stop loop cases.

## Not Implemented Yet

- Room entities/DAO/AppDatabase and offline cache persistence.
- City adapter runtime implementation.
- Production GTFS downloader/sync orchestration.
- Realtime ingestion.
- Transfer routing.
- Compose feature UI and ViewModels.

## Current Pass

`PASS 08B — DOCS_AND_DIAGRAMS_SYNC`

## Next Pass

`PASS 09 — RAKVERE_CITY_ADAPTER_METADATA`
