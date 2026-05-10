# CURRENT_STATE

## Repository Baseline

- Expected repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`.
- Expected branch: `main`.
- Last accepted HEAD before this pass: `280f8a0` (`PASS 11`).
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
- PASS 08B: docs/diagrams sync.
- PASS 09: Rakvere city adapter metadata.
- PASS 10: destination target model and place resolver.

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
- `city-adapters`:
  - Pure Kotlin metadata contract.
  - Rakvere metadata provider and city adapter registry.
  - Executable metadata tests.
- `feature-search`:
  - Destination target model and query normalizer.
  - Metadata-based place query resolver.
  - Place-to-stop candidate model and resolver (name-only candidate layer).
  - Origin candidate model and resolver (manual-text and current-location unresolved seeds).
  - Executable destination and candidate mapping tests.

## Not Implemented Yet

- Room entities/DAO/AppDatabase and offline cache persistence.
- City adapter runtime integration beyond metadata.
- Production GTFS downloader/sync orchestration.
- Realtime ingestion.
- Transfer routing.
- Compose feature UI and ViewModels.
- Verified stop-group/stop-point ID mapping from place candidates.
- Origin-to-stop-point nearest lookup.

## Current Pass

`PASS 12 — ORIGIN_STOPPOINT_CANDIDATE_RESOLVER_SPEC`

## Next Pass

`PASS 13 — DESTINATION_TO_DIRECT_ROUTE_QUERY_BRIDGE_SPEC`
