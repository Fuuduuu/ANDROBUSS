# TESTING_STRATEGY

## Current Test Baseline

- `core-domain` tests exist and run.
- `ServiceCalendarResolver` semantics are tested with explicit `LocalDate` inputs.
- `core-gtfs` CSV/parser/mapper tests exist and run.
- `core-routing` direct-route tests exist and run.
- `city-adapters` metadata tests exist and run.
- `feature-search` destination resolver tests exist and run.
- `feature-search` place-to-stop candidate mapping tests exist and run.
- `feature-search` origin candidate resolver tests exist and run.
- `feature-search` direct-route bridge/precondition tests exist and run.
- `feature-search` stop-point resolution contract/name-index tests exist and run.
- `feature-search` resolver-to-bridge integration tests (hand-built domain data) exist and run.
- `feature-search` stop-candidate enrichment production tests exist and run.
- `feature-search` destination enrichment orchestration tests exist and run.
- `feature-search` direct-route query preparation use-case tests exist and run.
- `feature-search` parser-to-search pipeline integration tests using `core-gtfs` `rakvere-smoke` fixture exist and run.
- `feature-search` feed snapshot/provider contract tests exist and run.
- CI baseline runs Gradle build/lint.

## Core-Domain Coverage Focus

- ID/display-name invariants.
- `GeoPoint` range validation.
- `RoutePattern` ordering and minimum-stop invariants.
- Duplicate stop-point compatibility for loop patterns.
- Calendar base + exception override semantics.

## Core-GTFS Coverage Focus

- CSV quoting/escaping and CRLF/LF behavior.
- Required-file validation.
- Calendar file presence rules.
- Exception mapping (`1` add, `2` remove, invalid values fail).
- StopPoint identity protection (same-name different-`stop_id` remains separate).

## Core-Routing Coverage Focus

- Direct-route validity rule (destination after origin in same pattern).
- Deterministic not-found precedence.
- Duplicate-stop loop handling.
- Ordered segment extraction.
- StopPointId-only identity behavior.

## City-Adapters Metadata Coverage Focus (PASS 09)

- Rakvere metadata presence and city identity.
- Wave assignment (`WAVE_0`).
- Primary and context feed mapping presence.
- Conservative legal status (no overclaim).
- Alias presence for basic Rakvere variants.
- POI seed non-empty with conservative coordinate policy.
- Registry lookup and duplicate city-id protection.
- Android-free API guard.
- PASS 17 real-stop-name discovery constraints:
  - at least one Rakvere POI has verified `preferredStopGroupNames`
  - any populated name must match discovered real `rakvere.zip` `stops.txt` values
  - uncertain POIs remain unresolved (empty preferred-stop-group list)
  - legal status remains conservative and coordinates remain `null`/`UNKNOWN`

## Feature-Search Destination Coverage Focus (PASS 10)

- Blank query handling (`BLANK_QUERY`).
- Unknown query handling (`NO_MATCH`).
- Exact display-name and alias matching.
- Case-insensitive and whitespace normalization behavior.
- Deterministic partial-match ordering.
- Rakvere metadata-backed matches (`Rakvere bussijaam`, `Vaala keskus`, `Kesklinn`).
- Destination target source (`CITY_PLACE_METADATA`) and conservative coordinate pass-through.
- Android-free resolver API guard.

## Feature-Search Place-to-Stop Candidate Coverage Focus (PASS 11)

- `NO_PREFERRED_STOP_GROUPS` behavior.
- `UNSUPPORTED_TARGET_SOURCE` behavior.
- One/multi preferred-stop-group candidate creation.
- Deterministic metadata-order preservation.
- No fabricated `StopPointId` values.
- Conservative candidate confidence policy.
- Android-free candidate resolver API guard.

## Feature-Search Origin Candidate Coverage Focus (PASS 12)

- Blank/whitespace manual text handling (`BLANK_QUERY`).
- Null current location handling (`MISSING_LOCATION`).
- Manual text candidate normalization and unresolved confidence policy.
- Current location coordinate pass-through without nearest-stop inference.
- No fabricated `StopPointId` or `StopGroup` mappings.
- Android-free origin resolver API guard.

## Feature-Search Direct-Route Bridge Coverage Focus (PASS 13)

- Not-ready precondition gating before route search call.
- Direct-route call guard when origin/destination/patterns are unresolved.
- Explicit route-search invocation only with provided `StopPointId` values.
- Deterministic first-ID selection policy.
- Name/coordinate anti-fabrication guarantees.
- Android-free bridge API guard.

## Feature-Search StopPoint Resolution Coverage Focus (PASS 14)

- Empty/unknown/index-missing result branches.
- Exact and normalized name-match confidence behavior.
- Multiple same-name `StopPoint` results in deterministic input order.
- Candidate fields sourced from actual `StopPoint` objects only.
- Future geospatial source/confidence placeholders are never emitted.
- Coordinate hint is accepted but ignored by PASS 14 name index.
- Android-free resolution API guard.

## Feature-Search Resolution-Bridge Integration Coverage Focus (PASS 15)

- Unresolved -> `NotReady` precondition path checks.
- Name-level candidate resolution via `InMemoryStopPointIndex`.
- Verified ID handoff into `DirectRouteQueryBridge`.
- `RouteFound` and `RouteNotFound` integration outcomes.
- Same-name stop points producing different route outcomes by `StopPointId`.
- No `StopPointId` fabrication from names.
- Guard tests proving unresolved cases do not trigger route search call.

## Feature-Search StopCandidate Enrichment Coverage Focus (PASS 16)

- Enrichment uses `StopPointResolver` output only.
- `StopCandidate.stopPointIds` are copied only from verified candidate IDs.
- Same-name multi-stop resolution preserves deterministic ID order.
- Failure branches preserve original unresolved candidate unchanged.
- No fabricated IDs on unknown/empty-index/error branches.
- Enrichment result exposes verified candidates for later ranking/wiring.
- Android-free guard for enrichment classes.

## Feature-Search Destination Enrichment Orchestration Coverage Focus (PASS 18)

- Empty-candidate branch returns `NoCandidates`.
- All-fail branch returns `NoneEnriched` with preserved failures.
- Enriched branch preserves all verified candidates and all failed candidates.
- Ambiguity semantics are deterministic:
  - `isAmbiguous=false` when total verified candidate count is exactly one.
  - `isAmbiguous=true` when total verified candidate count is greater than one.
- Orchestrator does not depend on or call `DirectRouteQueryBridge`.
- Anti-fabrication guarantees remain enforced through resolver+enricher pipeline.
- Android-free guard for orchestration classes.

## Feature-Search Route Query Preparation Coverage Focus (PASS 19)

- `NoCandidates` precondition branch.
- `DestinationUnresolved` branch.
- `DestinationAmbiguous` branch with bridge-call block.
- `OriginNotProvided` branch with bridge-call block.
- `NoPatternsAvailable` branch with bridge-call block.
- `Executed(RouteFound)` path when safe preconditions hold.
- `Executed(RouteNotFound)` path, including reverse-direction not-found semantics.
- Exact-one destination policy (`single()`) fails loudly on malformed non-ambiguous input.
- Bridge-call input preserves anti-fabrication destination `StopPointId`.
- Android-free guard for preparation use-case/result classes.

## Feature-Search Parser-to-Search Integration Coverage Focus (PASS 20)

- `GtfsFeedParser` + `GtfsDomainMapper` -> `MappedGtfsFeed` integration.
- `feature-search` uses `testImplementation(project(":core-gtfs"))` for this integration coverage only.
- Parser-derived `stopPoints` seed `InMemoryStopPointIndex`.
- Parser-derived `routePatterns` are consumed by `DirectRouteQueryPreparationUseCase`.
- `StopCandidateEnricher` and `DestinationEnrichmentOrchestrator` behavior on parser-derived names:
  - non-ambiguous (`Jaam`)
  - ambiguous (`Keskpeatus`)
- End-to-end parser-derived route-query preparation outcomes:
  - `RouteFound`
  - `RouteNotFound`
- Ambiguous parser-derived destination (`Keskpeatus`) blocks query preparation as `DestinationAmbiguous`.
- Loop pattern (`pattern:T3`) preservation from parser output.
- Anti-fabrication checks that route IDs come from parser/domain stop IDs, not name strings.

## Feature-Search Feed Snapshot Coverage Focus (PASS 21)

- `DomainFeedSnapshotProvider` city-match/null behavior.
- `DomainFeedSnapshot` preserves `stopPoints` and `routePatterns` unchanged.
- Snapshot `stopPoints` can seed `InMemoryStopPointIndex`.
- Snapshot `routePatterns` can drive `DirectRouteQueryPreparationUseCase`.
- Parser-agnostic boundary verification without importing parser types.
- Android-free guard for feed snapshot/provider classes.

## Feed Identity Strategy Coverage Focus (PASS 22A)

- Documented identity rule: GTFS `stop_id` and trip-derived pattern IDs are feed/city-local for persistence.
- Future Room entity keys must be validated as composite storage keys:
  - `cityId + feedId + stopId`
  - `cityId + feedId + patternId`
  - `cityId + feedId + patternId + sequence`
- Duplicate `stopId` values inside one pattern must remain preserved in persistence tests.

## Data-Local Room Baseline Coverage Focus (PASS 22B)

- `FeedEntityMapperTest` covers scoped key mapping and anti-fabrication:
  - stop-point round-trip
  - route-pattern round-trip
  - sequence preservation
  - duplicate stop IDs for loop patterns
  - same-name different-ID stop separation
- `FeedSnapshotDaoTest` covers scoped Room behavior:
  - city/feed-scoped queries
  - same local IDs across different scopes
  - ordered pattern-stop reads
  - cascade delete from route-pattern to pattern-stops
- `RoomDomainFeedSnapshotLoaderTest` covers load-then-serve provider baseline:
  - cache empty before `prepare`
  - `prepare(cityId, feedId)` populates cache
  - `getSnapshot(cityId)` serves cache only
  - route-pattern order and duplicate-stop preservation through Room load
  - route-search parity check using Room-loaded `routePatterns`

## Near-Term Test Gaps

- Production destination enrichment orchestration wiring into app/ViewModel flow (future).
- Room freshness/version metadata and lifecycle tests (future).
- Room-backed resolver parity tests (future).
- Room persistence/invalidation tests (future).
- UI and end-to-end flow tests (future).

## Governance Checks

- `tools/validate_project_state.py` validates `docs/PROJECT_STATE.yml` schema.
- Windows local: `py -3 tools/validate_project_state.py`
- CI/Linux: `python tools/validate_project_state.py`
- Validator uses Python stdlib only (`pathlib`, `re`, `subprocess`, `sys`).
- No `PyYAML` or other third-party dependency is required.
- Commit hash mismatch is warning-only, not a hard failure.
- GitHub Actions CI runs the validator.
