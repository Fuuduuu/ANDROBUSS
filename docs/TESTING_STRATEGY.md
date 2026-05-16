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
- `data-local` importer and parser-to-Room-to-provider integration tests exist and run.
- CI baseline runs Gradle build/test/lint.

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

## Data-Local Import Writer Coverage Focus (PASS 23)

- `FeedSnapshotImporterTest` covers:
  - domain snapshot write into Room
  - route-pattern/pattern-stop persistence
  - scoped replace behavior on repeated import for same city/feed
  - anti-fabrication (`StopPointId` remains persisted ID, never display-name derived)
  - same local `stopId` across different city/feed scopes
- `RoomFeedImportIntegrationTest` covers parser fixture to Room and back into query flow:
  - `GtfsFeedParser` + `GtfsDomainMapper` (test scope only)
  - `DomainFeedSnapshot` import via `FeedSnapshotImporter`
  - provider `prepare(cityId, feedId)` + cache `getSnapshot(cityId)`
  - Room-loaded stop points seeding `InMemoryStopPointIndex`
  - Room-loaded route patterns driving `DirectRouteQueryPreparationUseCase`
  - `RouteFound` and anti-fabrication assertions

## App Bootstrap Coverage Focus (PASS 25)

- `BootstrapFeedDtoTest` covers:
  - DTO -> `DomainFeedSnapshot` city/count mapping
  - anti-fabrication (`StopPointId` sourced from DTO `id`, never display name)
  - route-pattern stop order preservation
  - duplicate stop ID preservation for loop pattern data
- `FeedBootstrapLoaderTest` (Robolectric) covers:
  - bundled asset bootstrap import + provider prepare -> non-null snapshot
  - anti-fabrication checks for loaded stop IDs (`RKV_*`, not `"Jaam"`)
  - idempotent repeated bootstrap calls (no duplicate persisted rows)
  - missing-asset safe no-crash FeedNotReady-style behavior
- PASS 25 app runtime tests remain pre-Hilt and do not include UI/ViewModel/Compose/WorkManager.

## App Bootstrap Room-First Hardening Coverage (PASS_AUTO_04 Candidate)

- `FeedBootstrapLoaderTest` adds cold-start Room-first coverage:
  - when provider cache is empty but Room already has snapshot for bootstrap feed scope,
  - `bootstrapIfNeeded()` calls `prepare(cityId, feedId)` and uses Room-loaded snapshot,
  - bundled asset fallback is not required for this path.
- Missing-asset behavior remains safe:
  - with empty Room and missing asset, no crash and snapshot remains null.
- Idempotency remains verified:
  - repeated `bootstrapIfNeeded()` calls do not duplicate persisted rows.

## Detekt Boundary Coverage Extension (PASS_AUTO_05 Accepted)

- Boundary-only Detekt coverage is extended to:
  - `app`
  - `data-local`
  - `feature-search`
  - `city-adapters`
- Rule scope remains forbidden-import boundary checks only:
  - no style/complexity enforcement in this pass.
- Validation command set:
  - `.\gradlew.bat :app:detekt`
  - `.\gradlew.bat :data-local:detekt`
  - `.\gradlew.bat :feature-search:detekt`
  - `.\gradlew.bat :city-adapters:detekt`
  - `.\gradlew.bat detekt`
- Manual grep checks remain required for explicit boundary assertions.

## App DI Baseline Coverage Focus (PASS 27)

- `:app:test` validates Hilt baseline integration at compile/runtime unit-test level:
  - `@HiltAndroidApp` application compiles and test tasks run,
  - app bootstrap tests still pass with injected bootstrap dependencies,
  - runtime default synthetic asset behavior remains unchanged.
- PASS 27 does not add ViewModel/UI Hilt tests.
- PASS 27 does not add WorkManager/network/realtime tests.

## App Search ViewModel State Coverage Focus (PASS 28A)

- `SearchViewModelTest` covers:
  - initial `FeedState.NotReady` + empty destination input state,
  - `refreshFeedState()` transitions (`Ready` / `NotReady`) from provider snapshot availability,
  - destination text handling (`Empty`, `Typed`),
  - non-ambiguous destination resolution (`Jaam` -> `RKV_C`) without ID fabrication,
  - ambiguous destination resolution (`Keskpeatus` -> multiple options) without auto-select,
  - explicit ambiguous option selection to a single resolved option,
  - feed-not-ready destination input safety (no crash, no fabricated IDs).
- PASS 28A does not add Compose/navigation/UI tests.

## App Route Query + Explicit Origin Coverage Focus (PASS 28B Accepted)

- `SearchViewModelTest` now covers route-query baseline behavior:
  - `searchRoute()` with no origin -> `OriginNotProvided` (not `RouteNotFound`),
  - `searchRoute()` with feed unavailable -> `FeedNotAvailable` (not `RouteNotFound`),
  - `searchRoute()` with ambiguous destination -> `DestinationNotReady` (not `RouteNotFound`),
  - verified destination `StopPointId` path is used for query (not display-name fabrication),
  - route query path uses `DirectRouteQueryPreparationUseCase`,
  - ambiguous destination is never auto-selected.
- Route-query precondition states are explicitly distinct from `RouteNotFound`:
  - `FeedNotAvailable`
  - `DestinationNotReady`
  - `OriginNotProvided`
  - `NoPatternsAvailable`
- PASS 28B remains ViewModel/state-only:
  - no Compose/navigation/UI tests,
  - no GPS/nearest-stop tests,
  - no network/realtime tests.

## App Compose Search Screen Baseline Coverage (PASS 28C Accepted)

- PASS 28C adds first app Compose screen wiring without navigation graph.
- Baseline tests are lightweight state-to-text mapping checks:
  - feed status wording remains static schedule-safe
  - route-found headline rendering text is stable
  - destination prompt keeps explicit "Vali sihtkoht" action
- PASS 28C keeps behavior constraints:
  - destination resolution is not triggered on every keystroke
  - route query remains explicit button action
  - origin selection stays temporary dev-only chips (no GPS/permissions)

## Drift/Boundary Verification (PASS_AUTO_06 Accepted)

- After PASS 28C acceptance, run docs-only governance drift sync before opening new UI scope.
- Required verification set:
  - `.\gradlew.bat detekt`
  - `.\gradlew.bat test`
  - `.\gradlew.bat build`
  - parser/leak/Hilt/navigation/GPS/network/realtime boundary greps
  - `py -3 tools/validate_project_state.py`

## Search Screen Smoke And Polish Coverage (PASS_UI_01 Accepted)

- `SearchScreenStateTextTest` covers first-screen polish guards:
  - search button disabled when destination is unresolved,
  - search button disabled when origin is missing,
  - search button enabled only when destination is resolved and origin is selected,
  - `FeedState.Ready` banner hidden behavior,
  - `DestinationNotReady` message avoids technical "route-otsinguks" wording,
  - route-found summary text avoids raw machine ID exposure.

## Rakvere Quick Destination Readiness Coverage (PASS_29A Accepted)

- `RakvereQuickDestinationReadinessTest` validates readiness boundaries and runtime-label policy:
  - real static runtime baseline contains expected quick-label coverage (`Rakvere bussijaam`, `Polikliinik`, `Näpi`, `Keskväljak`, `Põhja`),
  - `Tõrma` is absent from runtime stop display names,
  - `SearchViewModel` resolves labels/query text only when present in active snapshot,
  - anti-fabrication checks keep StopPoint identity ID-based, not label-derived.

## Quick Destination Runtime Policy Gates (PASS_29C Accepted)

- Before any quick-destination UI implementation is accepted:
  - labels/query text must resolve through normal destination flow in the active runtime snapshot,
  - resolved identity must come from verified `StopPoint.id` (`VerifiedStopPointCandidate.stopPointId`),
  - ambiguous results must stay user-selectable (no auto-pick),
  - no UI constants may inject `StopPointId` directly,
  - test-only real profile fixtures must not be treated as runtime truth.

## Real Static Runtime Baseline Coverage (PASS_30 Accepted)

- `FeedBootstrapLoaderTest` now covers runtime bootstrap primary/fallback behavior:
  - default bootstrap loads real static runtime asset (`rakvere_feed_20260428.json`),
  - Room prepare for primary feed is attempted before any asset import,
  - synthetic fallback (`rakvere_bootstrap.json`) is used when primary asset is missing,
  - missing primary + missing fallback stays safe (`FeedNotReady` style no-crash result),
  - anti-fabrication checks ensure stop IDs come from asset IDs, not display names.
- `RakvereQuickDestinationReadinessTest` now validates runtime-like real label readiness:
  - runtime static profile contains real Rakvere labels,
  - `SearchViewModel` resolves `Rakvere bussijaam` against runtime-like snapshot,
  - synthetic fallback remains synthetic when loaded explicitly.

## Quick Destinations UI Coverage (PASS_31 Accepted)

- `SearchScreenStateTextTest` now covers quick-destination UI wiring:
  - `Kiirvalikud` labels include `Rakvere bussijaam`, `Polikliinik`, `Näpi`, `Keskväljak`, `Põhjakeskus`,
  - `Tõrma` is intentionally excluded,
  - `Põhjakeskus` chip maps to query text `Põhja`,
  - quick-destination selection calls destination resolver callback only,
  - quick-destination click does not trigger route-search callback directly.

## Drift/UI Boundary Verification (PASS_AUTO_07 Accepted)

- Post-PASS-31 governance checkpoint verifies:
  - no parser type leakage in app/data-local production code,
  - no `@Serializable` in `core-domain`,
  - no `allowMainThreadQueries` in `data-local`,
  - no Hilt annotation leaks to `core-*`/`feature-search`,
  - quick-destination path remains label/query-text only,
  - no GPS/network/realtime/navigation/WorkManager expansion.

## Runtime Origin Candidate Coverage (PASS_32 Candidate)

- `SearchViewModelTest` now adds runtime-origin readiness checks:
  - `originCandidates` are empty when feed is not ready,
  - `originCandidates` are built from runtime-like snapshot data and include `Rakvere bussijaam` + `Polikliinik`,
  - every origin option `StopPointId` exists in snapshot stop points and appears in at least one route pattern,
  - old synthetic origin IDs (`RKV_A_OUT`, `RKV_A_IN`, `RKV_B`, `RKV_C`) are absent from runtime-origin candidate output,
  - selecting an origin option stores concrete `originStopPointId`,
  - route query with runtime-origin option does not return `ORIGIN_NOT_FOUND`.
- `SearchScreenStateTextTest` now adds origin-section behavior checks:
  - runtime-backed preferred origin labels are present,
  - synthetic legacy origin labels are absent from preferred origin ordering,
  - multi-option origin groups require explicit option selection (no implicit first-stop auto-pick),
  - single-option group tap returns a concrete `StopPointId` selection event.

## Near-Term Test Gaps

- Production destination enrichment orchestration wiring into app/ViewModel flow (future).
- Room freshness/version metadata and lifecycle tests (future).
- Room-backed resolver parity tests (future).
- Room persistence/invalidation tests (future).
- UI and end-to-end flow tests (future).

## PASS 26 Future Required Tests (Real Rakvere Readiness)

- Quoted `service_id` values with commas must parse correctly.
- Unknown GTFS columns must be tolerated without parser failure.
- `calendar_dates` exception behavior must be verified against base calendar.
- `stop_area = "Rakvere linn"` filtering policy must be explicit and test-covered.
- Raw ZIP commit guard must remain enforced (no `rakvere.zip` committed).
- `block_id` presence must be tolerated but must not activate routing behavior.
- If real-asset policy uses Rakvere `stop_area` scope, generated asset tests must confirm non-Rakvere stops are excluded.

## PASS 26A Parser Robustness Coverage

- New fixture: `core-gtfs/src/test/resources/gtfs/rakvere-profile-smoke/`
- Coverage focus:
  - quoted `service_id` values with commas preserved across trips/calendar/calendar_dates,
  - unknown/extra GTFS columns tolerated without parser failure,
  - missing optional files (`frequencies.txt`, `transfers.txt`, `attributions.txt`) tolerated,
  - `calendar_dates` `exception_type=1/2` behavior verified through `ServiceCalendarResolver`,
  - loop-pattern duplicate stop preservation (`STOP_A -> STOP_B -> STOP_A`),
  - explicit anti-fabrication checks (`StopPointId` from `stop_id` only),
  - explicit assertion that `stop_area` filtering is not parser responsibility.

## PASS 26B Real-derived Dev/Test Asset Coverage

- Test-only real-derived asset location:
  - `app/src/test/resources/bootstrap/rakvere_dev_profile_v1.json`
- Coverage focus:
  - asset decodes through `BootstrapFeedDto` -> `DomainFeedSnapshot`,
  - identity markers are stable (`cityId = rakvere`, `feedId` dev/profile),
  - expected profile size is verified (`98` stops, `7` route patterns),
  - anti-fabrication checks ensure stop IDs come from `id` fields, not display names,
  - route-pattern IDs are non-synthetic and references point to existing stop IDs,
  - runtime default remains synthetic main asset (`app/src/main/assets/bootstrap/rakvere_bootstrap.json`).
- Policy guard:
  - real-derived asset remains in test resources only and is not production runtime default.

## Governance Checks

- `tools/validate_project_state.py` validates `docs/PROJECT_STATE.yml` schema.
- Windows local: `py -3 tools/validate_project_state.py`
- CI/Linux: `python tools/validate_project_state.py`
- Validator uses Python stdlib only (`pathlib`, `re`, `subprocess`, `sys`).
- No `PyYAML` or other third-party dependency is required.
- Commit hash mismatch is warning-only, not a hard failure.
- GitHub Actions CI runs the validator.
- GitHub Actions CI also runs `./gradlew test`.
- Docs-only memory/read-order passes should validate with:
  - `py -3 tools/validate_project_state.py`
  - `git diff --check`
  - `git status --short --untracked-files=all`
- Runtime/module tests are not required for docs-only passes unless source/build/runtime files changed unexpectedly.

## Dependency Locking Checks (PASS_AUTO_02)

- Gradle dependency locking is enabled across all modules.
- Lockfiles (`gradle.lockfile`) are version-controlled and should change only in explicit dependency passes.
- Drift detection command for lock refresh:
  - `.\gradlew.bat build --write-locks`

## Drift And Boundary Checks (PASS_AUTO_03)

- After PASS 27 acceptance, run docs-only drift verification before ViewModel scope expansion.
- Required boundary verification includes:
  - `.\gradlew.bat detekt`
  - `.\gradlew.bat test`
  - `.\gradlew.bat build`
  - parser/leak and Hilt-boundary grep checks across `app/src/main`, `data-local/src/main`, `core-*`, and `feature-search`.
- PASS 28 scope audit should start only when AUTO-03 drift/boundary checks are clean.
