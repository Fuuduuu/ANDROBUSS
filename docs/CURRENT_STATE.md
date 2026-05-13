# CURRENT_STATE

## Repository Baseline

- Expected repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Expected branch: `main`
- Latest accepted HEAD: `1f11ec3` (`PASS_27_HILT_DI_BASELINE`)
- Working tree must be clean before a new pass

## Latest Accepted Pass

- `PASS_27 — HILT_DI_BASELINE`
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
- Runtime default remains synthetic `app/src/main/assets/bootstrap/rakvere_bootstrap.json`.
- PASS 27 accepted state introduces app-owned Hilt DI modules for:
  - `AppDatabase` / `FeedSnapshotDao`
  - `FeedSnapshotImporter`
  - `RoomDomainFeedSnapshotLoader`
  - `RoomDomainFeedSnapshotProvider`
  - `FeedBootstrapLoader`
- `AndrobussApplication` is now `@HiltAndroidApp` and triggers bootstrap via injected loader.
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
- ViewModel/Compose runtime wiring of the pipeline
- Nearest-stop/geospatial resolution
- UI feature flows

## Current Risks

- Runtime bootstrap is Hilt-backed but remains anchored in `Application.onCreate` until ViewModel/runtime lifecycle ownership pass.
- Room baseline exists, but freshness metadata and feed lifecycle evolution are not implemented.
- Production `RoutePattern` source is not implemented.
- UI/ViewModel wiring is not implemented.
- Nearest-stop/geospatial behavior is not implemented.
- `rakvere-smoke` names are synthetic and separate from real Rakvere POI metadata names.
- First-launch `FeedNotReady` state is expected before bootstrap prepare and is not an error state.

## Current Pass

- `PASS_AUTO_03 — DRIFT_AND_BOUNDARY_CHECK` (docs-only governance sync after accepted PASS 27)

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

- `PASS_28 — SEARCH_VIEWMODEL_AND_FEED_STATE_SCOPE_AUDIT`
- `PASS_28A — SEARCH_VIEWMODEL_AND_FEED_STATE` only after PASS_28 scope lock.
- Production real asset remains blocked by legal/freshness/update-policy constraints.
