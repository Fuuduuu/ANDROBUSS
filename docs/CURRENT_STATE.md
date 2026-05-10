# CURRENT_STATE

## Repository Baseline

- Expected repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`.
- Expected branch: `main`.
- Latest accepted HEAD: `ebd1ff3` (`PASS 17`).
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
- PASS 11B: stop-candidate confidence docs clarification.
- PASS 12: origin candidate resolver.
- PASS 13: direct-route query bridge and precondition gating.
- PASS 14: stop-point resolution contract and in-memory name index.
- PASS 15: stop resolution and bridge integration tests.
- PASS 16: stop-candidate enrichment production class.
- PASS 16B: enrichment docs/diagrams sync.
- PASS 17: Rakvere real GTFS stop-name discovery and conservative metadata mapping.

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
  - PASS 17 adds conservative preferred-stop-group-name mappings from real `rakvere.zip` `stops.txt`.
  - `city-adapters` now depends on `core-domain` only (no `core-gtfs` dependency).
  - Executable metadata tests.
- `feature-search`:
  - Destination target model and query normalizer.
  - Metadata-based place query resolver.
  - Place-to-stop candidate model and resolver (name-only candidate layer).
  - Origin candidate model and resolver (manual-text and current-location unresolved seeds).
  - Direct-route query bridge with explicit precondition gating before route search calls.
  - Stop-point resolution contract and in-memory name index for verified `StopPointId` candidates.
  - Stop-candidate enrichment production class (`StopCandidateEnricher`) for destination-side ID enrichment.
  - Destination enrichment orchestration production class (`DestinationEnrichmentOrchestrator`).
  - Orchestrator does not call `DirectRouteQueryBridge` and does not select a single verified stop-point candidate.
  - Orchestrator returns ambiguity flag when multiple verified stop-point candidates exist.
  - Enrichment orchestration is not yet wired into app/ViewModel runtime flow.
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
- No UI runtime implementation exists yet.

## Current Pass

`PASS 18 — DESTINATION_ENRICHMENT_ORCHESTRATOR_AND_AMBIGUITY_SPEC`

## Next Technical Pass

`PASS 19 — DESTINATION_ROUTE_QUERY_PREPARATION_USE_CASE`
