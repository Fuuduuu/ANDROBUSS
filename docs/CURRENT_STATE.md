# CURRENT_STATE

## Repository Baseline

- Expected repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Expected branch: `main`
- Latest accepted HEAD: `cf7eb79` (`PASS_32_ORIGIN_PICKER_FROM_RUNTIME_STOPS_GROUP_AWARE`)
- Working tree must be clean before a new pass

## Latest Accepted Pass

- `PASS_28C — COMPOSE_SEARCH_SCREEN_BASELINE` (accepted first visible UI baseline pass)
- `PASS_AUTO_06 — DRIFT_AND_UI_BASELINE_CHECK` (accepted docs-only drift + UI boundary verification)
- `PASS_UI_01 — SEARCH_SCREEN_SMOKE_AND_POLISH` (accepted first-screen polish guard pass)
- `PASS_29A — RAKVERE_QUICK_DESTINATION_RESOLUTION_READINESS` (accepted readiness evidence pass)
- `PASS_29C — REAL_RUNTIME_FEED_POLICY_BEFORE_QUICK_DESTINATIONS` (accepted protected-surface policy gating pass)
- `PASS_30 — REAL_RAKVERE_STATIC_RUNTIME_PROFILE_BASELINE` (accepted internal/MVP runtime primary profile pass)
- `PASS_31 — RAKVERE_QUICK_DESTINATIONS_UI_ONLY` (accepted quick-chip UI-only pass)
- `PASS_AUTO_07 — DRIFT_AND_UI_BOUNDARY_CHECK` (accepted docs-only drift/boundary verification)
- `PASS_32 — ORIGIN_PICKER_FROM_RUNTIME_STOPS_GROUP_AWARE` (accepted runtime-backed origin selection implementation)
- Current technical pass: `PASS_33 — ORIGIN_SEARCH_DIALOG` (origin search dialog implementation)
- Latest docs-only governance/future-notes pass remains `PASS_G05 — GTFS_REALTIME_AND_PEATUS_GRAPHQL_FUTURE_NOTES`

PASS 21 added a parser-agnostic feed boundary and in-memory provider bootstrap.

PASS 22A confirms storage-identity strategy for future Room baseline:
- GTFS `stop_id` and `tripId`-derived pattern IDs are treated as feed/city-local identifiers.
- Future storage keys must be city/feed-scoped:
  - stop-point key: `cityId + feedId + stopId`
  - route-pattern key: `cityId + feedId + patternId`
  - pattern-stop key: `cityId + feedId + patternId + sequence`

## Current Core Status

- `core-domain`, `core-gtfs`, `core-routing`, `city-adapters`, and `feature-search` pure Kotlin search stack are implemented and tested.
- `DomainFeedSnapshot` and `DomainFeedSnapshotProvider` now live in `core-domain`.
- `feature-search` keeps `InMemoryDomainFeedSnapshot` as an in-memory implementation.
- `data-local` now includes Room baseline schema + DAO + mapper + load-then-serve provider using scoped keys:
  - stop-point key: `cityId + feedId + stopId`
  - route-pattern key: `cityId + feedId + patternId`
  - pattern-stop key: `cityId + feedId + patternId + sequence`
- `feature-search` has test-scope parser integration only:
  - `testImplementation(project(":core-gtfs"))`
- No production parser dependency from feature-search runtime code.
- `RoomDomainFeedSnapshotProvider` caches snapshots by `CityId` and is prepared by explicit `prepare(cityId, feedId)` calls.
- PASS 23 added production `FeedSnapshotImporter` so domain snapshots can be written into Room.
- PASS 23 also added parser -> domain snapshot -> Room -> provider -> search-pipeline integration coverage and CI `./gradlew test` step.
- PASS 24 documents MVP runtime bootstrap policy: bundled APK feed asset first, runtime downloader later.
- PASS 25 implements the pre-Hilt app bootstrap baseline:
  - bundled synthetic JSON bootstrap asset
  - app DTO -> `DomainFeedSnapshot` mapper
  - `FeedBootstrapLoader` (`import` + `prepare`) on app startup
  - `AndrobussApplication` runtime wiring and `AppDatabase.create(context)` factory
  - Robolectric tests for bootstrap success/idempotency/missing-asset safety/anti-fabrication
- PASS 26 (docs-only decision) documents legal/source/freshness constraints before any real Rakvere asset generation.
- Synthetic bundled bootstrap asset remains active; real Rakvere asset is not generated in repository state.
- PASS 26A adds executable parser robustness fixture/tests for real-feed profile characteristics (quoted service IDs, unknown columns, optional-file absence, calendar exceptions, loop duplicate stops).
- PASS 26B accepted state includes real-derived Rakvere dev/test profile asset under `app/src/test/resources` only.
- PASS 30 accepted promotion keeps `app/src/main/assets/bootstrap/rakvere_feed_20260428.json` as runtime primary baseline for internal/MVP use.
- Synthetic `app/src/main/assets/bootstrap/rakvere_bootstrap.json` remains fallback asset.
- PASS 27 accepted state introduces app-owned Hilt DI modules for:
  - `AppDatabase` / `FeedSnapshotDao`
  - `FeedSnapshotImporter`
  - `RoomDomainFeedSnapshotLoader`
  - `RoomDomainFeedSnapshotProvider`
  - `FeedBootstrapLoader`
- `AndrobussApplication` is now `@HiltAndroidApp` and triggers bootstrap via injected loader.
- PASS 28A adds first app presentation state baseline:
  - `SearchViewModel` in app module
  - `SearchUiState` with `FeedState` and `DestinationInputState`
  - destination enrichment uses existing feature-search pure Kotlin resolver/orchestrator components
  - no UI/navigation wiring
- PASS 28B accepted state extends SearchViewModel with explicit route-query state baseline:
  - `RouteQueryState` added with separate `FeedNotAvailable`, `DestinationNotReady`, `OriginNotProvided`, `NoPatternsAvailable`, and `RouteNotFound`
  - `searchRoute()` is explicit trigger only (no auto-run on input/origin changes)
  - explicit origin is required for route query
  - route query uses `DirectRouteQueryPreparationUseCase`
  - no UI/GPS/nearest-stop/network/realtime scope opened
- PASS_AUTO_04 accepted state hardens bootstrap lifecycle:
  - cold start now checks provider cache, then `prepare(cityId, feedId)` from Room, and only then falls back to bundled import
  - bundled synthetic asset remains fallback path, not first choice when Room already has bootstrap snapshot
- PASS_AUTO_05 accepted state extends Detekt boundary coverage to:
  - `app`
  - `data-local`
  - `feature-search`
  - `city-adapters`
  while keeping Detekt scope boundary-only (no default style/complexity bleed)
- PASS_28C accepted state adds first Compose search screen baseline in app:
  - `MainActivity` sets Compose content and is `@AndroidEntryPoint`
  - Search screen uses existing `SearchViewModel` via `hiltViewModel()`
  - destination input uses local text state with explicit "Vali sihtkoht" action
  - route search remains explicit via separate "Otsi" action
  - origin selector baseline existed as MVP/dev-only hardcoded chips (no GPS/permissions)
- First visible UI now exists in app, but it remains minimal MVP/diagnostic baseline.
- No navigation graph, GPS/map permissions, downloader/network, WorkManager, or realtime behavior has been opened.
- Runtime bootstrap policy uses real-static primary + synthetic fallback while keeping production freshness unresolved.
- PASS 31 accepted state adds quick-destination chips in Compose search screen:
  - chips pass label/query-text only into `onDestinationChanged(queryText)`,
  - no `StopPointId` shortcuts are used in quick-chip path,
  - quick-chip clicks do not call `searchRoute()` directly,
  - ambiguity handling remains unchanged (no auto-selection).
- PASS 32 accepted state replaces synthetic origin chips with runtime snapshot-backed origin candidates:
  - origin candidates are built from active `DomainFeedSnapshot.stopPoints` + `routePatterns`,
  - candidates are grouped by `stopGroupId`,
  - multi-stop groups require explicit concrete option selection (no implicit first-stop auto-pick),
  - old synthetic IDs (`RKV_A_OUT`, `RKV_A_IN`, `RKV_B`, `RKV_C`) are removed from app UI origin section.
- PASS 33 candidate adds local UI origin search dialog behavior:
  - `OriginSearchDialog` uses local Compose state (no ViewModel dialog flags),
  - search filters existing runtime-backed `originCandidates` by group display name,
  - multi-option groups expand first and require explicit option selection,
  - option selection uses existing `OriginCandidateOption.stopPointId`.
- Governance/tooling guardrails accepted after PASS 25:
  - PASS G03 audit-index/read-order sync
  - PASS AUTO-01 detekt module-boundary checks
  - DRIFT_CHECK_RULE_SYNC_PASS
  - PASS AUTO-02 dependency locking
- Full-project audit checks are healthy at `3bb2fe1`:
  - `./gradlew detekt` passed
  - `./gradlew test` passed
  - `./gradlew build` passed

## Not Implemented Yet

- Production route-pattern source/provider wiring
- Production feed downloader/refresh flow
- Compose/navigation runtime wiring of the pipeline
- Nearest-stop/geospatial resolution
- UI feature flows

## Current Risks

- Runtime bootstrap is Hilt-backed but remains anchored in `Application.onCreate` until ViewModel/runtime lifecycle ownership pass.
- Room baseline exists, but freshness metadata and feed lifecycle evolution are not implemented.
- Production `RoutePattern` source is not implemented.
- Compose UI wiring exists as a single-screen MVP baseline and remains incomplete for production UX flows.
- Nearest-stop/geospatial behavior is not implemented.
- `rakvere-smoke` names are synthetic and separate from real Rakvere POI metadata names.
- First-launch `FeedNotReady` state is expected before bootstrap prepare and is not an error state.

## Current Pass

- `PASS_33 — ORIGIN_SEARCH_DIALOG`

## Lazy Context Note (PASS G03)

- `docs/AUDIT_INDEX.md` is the lightweight lazy-context manifest for pass/audit history.
- Full `docs/audit/PASS_*.md` files are detail sources and should be opened on-demand, not as default context.
- `docs/archive/**` is historical-only context and should not be loaded by default.

## Governance Bootstrap (PASS_G01)

- Added compact governance scaffold docs:
  - `docs/PROJECT_STATE.yml`
  - `docs/INVARIANTS.md`
  - `docs/DEBUG_PLAYBOOK.md`
  - `docs/PASS_TEMPLATE.md`
- No runtime/source/build behavior changes.

## Governance Validation (PASS_G02)

- Added `tools/validate_project_state.py` for compact `docs/PROJECT_STATE.yml` schema checks.
- Validator is runnable locally and is wired into CI.
- Local Windows validator command: `py -3 tools/validate_project_state.py`.

## Dependency Locking (PASS_AUTO_02 Accepted)

- Gradle dependency locking is enabled project-wide (`lockAllConfigurations()`).
- Generated `gradle.lockfile` files are version-controlled per module, plus `settings-gradle.lockfile`.
- Dependency updates now require explicit lock refresh in a dedicated pass.
- No runtime behavior changes were introduced by locking.

## Next Technical Pass

- `PASS_34_ORIGIN_SELECTION_POLISH_SCOPE_AUDIT`
- Alternative: `PASS_FEED_01_DOWNLOADER_FRESHNESS_SCOPE_AUDIT`
- Alternative: `PASS_UI_02_SEARCH_SCREEN_POLISH_SCOPE_AUDIT`
- Public/freely distributed production still requires downloader/update/freshness policy.

## Runtime Feed Policy Note (PASS 30 Baseline Target)

- Current PASS 30 implementation target is real static runtime baseline for internal/MVP use:
  - primary asset: `app/src/main/assets/bootstrap/rakvere_feed_20260428.json`
  - fallback asset: `app/src/main/assets/bootstrap/rakvere_bootstrap.json`
- This is not a public-production freshness solution.
- Metadata alone still does not bypass resolver identity rules.
- Verified routing identity must come from active runtime `StopPoint.id` resolution path, not UI-supplied IDs.
- PASS 31 now adds quick-destination chips using label/query-text pairs only.
- `Põhjakeskus` chip is backed by query text `Põhja` in current runtime baseline.
- `Tõrma` remains excluded pending metadata/alias policy pass.
- Safe future path remains: quick label/query text -> `SearchViewModel.onDestinationChanged(queryText)` -> existing resolver flow.
