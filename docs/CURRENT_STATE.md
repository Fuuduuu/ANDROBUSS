# CURRENT_STATE

## Repository Baseline

- Expected repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`.
- Expected branch: `main`.
- Last accepted HEAD before this pass: `b35db77` (`PASS UX-01`).
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
- PASS 11: place-to-stop candidate mapping.
- PASS 12: origin candidate resolver.
- PASS 13: direct-route query bridge and precondition gating.
- PASS 14: stop-point resolution contract and in-memory name index.
- PASS 15: stop resolution and bridge integration tests.
- PASS 16: stop-candidate enrichment production class.

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
  - Direct-route query bridge with explicit precondition gating before route search calls.
  - Stop-point resolution contract and in-memory name index for verified `StopPointId` candidates.
  - Stop-candidate enrichment production class (`StopCandidateEnricher`) for destination-side ID enrichment.
  - Enrichment class is not yet wired into app/ViewModel runtime flow.
  - Integration tests for resolver-to-bridge flow using hand-built domain data.
  - Executable destination, candidate mapping, and integration tests.

## Not Implemented Yet

- Room entities/DAO/AppDatabase and offline cache persistence.
- City adapter runtime integration beyond metadata.
- Production GTFS downloader/sync orchestration.
- Realtime ingestion.
- Transfer routing.
- Compose feature UI and ViewModels.
- Verified stop-group/stop-point ID mapping from place candidates.
- Origin-to-stop-point nearest lookup.
- Room-backed stop-point resolver replacement.
- Production wiring for stop-point resolution into bridge inputs.
- Origin enrichment production class is deferred.

## UX Planning State

- Gemini UX research is integrated as planning guidance.
- Destination-first and list-first/home-map-second rules are now canonical planning inputs.
- No runtime/UI code was added in this UX sync pass.

## Current Pass

`PASS 16 — STOP_CANDIDATE_ENRICHMENT_PRODUCTION`

## Next Technical Pass

`PASS 17 — RAKVERE_STOPPOINT_MAPPING_FIXTURE_SPEC`
