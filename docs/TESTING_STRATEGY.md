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

## Near-Term Test Gaps

- Production destination enrichment orchestration wiring into app/ViewModel flow (future).
- Route-query preparation tests after ambiguity handling (PASS 19 target).
- Room-backed resolver parity tests (future).
- Room persistence/invalidation tests (future).
- UI and end-to-end flow tests (future).
