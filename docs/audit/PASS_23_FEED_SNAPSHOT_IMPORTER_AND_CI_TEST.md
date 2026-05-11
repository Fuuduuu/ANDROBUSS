# PASS_23_FEED_SNAPSHOT_IMPORTER_AND_CI_TEST

## Objective

Add the missing production write path from `DomainFeedSnapshot` into Room, prove import/load roundtrip behavior (including parser fixture integration), and add CI `./gradlew test` coverage.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- `git branch --show-current`: `main`
- `git remote -v`: `https://github.com/Fuuduuu/ANDROBUSS.git` (fetch/push)
- `git rev-parse HEAD`: `49dd54dff5861d60fee04703a227823eae19a93b`
- `git status --short --untracked-files=all`: clean before edits
- `py -3 tools/validate_project_state.py`: passed

## Files Read

- Governance and scope docs:
  - `README.md`
  - `AGENTS.md`
  - `docs/PROJECT_STATE.yml`
  - `docs/INVARIANTS.md`
  - `docs/CURRENT_STATE.md`
  - `docs/TRUTH_INDEX.md`
  - `docs/PROTECTED_SURFACES.md`
  - `docs/CODEBASE_IMPACT_MAP.md`
  - `docs/ROADMAP.md`
  - `docs/GTFS_PIPELINE.md`
  - `docs/ROUTING_LOGIC.md`
  - `docs/TESTING_STRATEGY.md`
  - `docs/ANDROID_ARCHITECTURE.md`
  - `docs/audit/PASS_22B_FEED_CONTRACT_MOVE_AND_ROOM_SCHEMA_WITH_SCOPED_KEYS.md`
- Data-local baseline:
  - `data-local/build.gradle.kts`
  - `data-local/src/main/kotlin/ee/androbus/data/local/dao/FeedSnapshotDao.kt`
  - `data-local/src/main/kotlin/ee/androbus/data/local/database/AppDatabase.kt`
  - `data-local/src/main/kotlin/ee/androbus/data/local/entity/*`
  - `data-local/src/main/kotlin/ee/androbus/data/local/mapping/FeedEntityMapper.kt`
  - `data-local/src/main/kotlin/ee/androbus/data/local/provider/RoomDomainFeedSnapshotLoader.kt`
  - `data-local/src/main/kotlin/ee/androbus/data/local/provider/RoomDomainFeedSnapshotProvider.kt`
  - existing `data-local/src/test/kotlin/**`
- Integration surfaces:
  - `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
  - `core-gtfs/src/main/kotlin/ee/androbus/core/gtfs/**`
  - `core-gtfs/src/test/resources/gtfs/rakvere-smoke/**`
  - `feature-search/src/main/kotlin/ee/androbus/feature/search/**`
  - `feature-search/src/test/kotlin/ee/androbus/feature/search/integration/**`
  - `.github/workflows/ci.yml`

## DAO replaceSnapshot Status

- `FeedSnapshotDao` did not have scoped transaction replace before PASS 23.
- Added `@Transaction suspend fun replaceSnapshot(...)` to:
  - delete existing route patterns for `cityId + feedId` scope
  - delete existing stop points for `cityId + feedId` scope
  - insert stop points
  - insert route patterns
  - insert pattern stops
- Entity schema was not changed in PASS 23.

## Importer Added

- Added production `data-local` class:
  - `FeedSnapshotImporter`
- Behavior:
  - accepts `DomainFeedSnapshot` + explicit `CityId` + explicit `FeedId`
  - maps domain objects through `FeedEntityMapper`
  - writes through DAO `replaceSnapshot(...)`
- Parser boundary:
  - importer uses domain types only
  - no production import of `MappedGtfsFeed`, `GtfsFeedParser`, or `GtfsDomainMapper`

## Test Dependency Scope

- `data-local` production dependency on `core-gtfs` removed.
- Added test-scope dependencies:
  - `testImplementation(project(":core-gtfs"))` for parser fixture integration test
  - `testImplementation(project(":feature-search"))` for end-to-end query-preparation assertions from Room-loaded snapshot

## Tests Added

### `FeedSnapshotImporterTest`

- import writes stop points into Room
- import writes route patterns and pattern stops (order preserved)
- repeated import for same city/feed replaces prior scoped data (no stale rows)
- anti-fabrication check (`StopPointId("RKV_C")` remains ID, never `StopPointId("Jaam")`)
- same local stop ID across different city/feed scopes remains independent

### `RoomFeedImportIntegrationTest`

Full roundtrip proof using `rakvere-smoke` fixture:

1. parse fixture with `GtfsFeedParser`
2. map with `GtfsDomainMapper`
3. convert to `DomainFeedSnapshot`
4. import via `FeedSnapshotImporter`
5. load via `RoomDomainFeedSnapshotProvider.prepare(...)` + `getSnapshot(...)`
6. assert loaded stop IDs include `RKV_A_OUT`, `RKV_A_IN`, `RKV_B`, `RKV_C`
7. assert loaded route patterns include `pattern:T1` and `pattern:T3`
8. seed `InMemoryStopPointIndex` from loaded stop points
9. resolve `"Jaam"` to `RKV_C`
10. run `DirectRouteQueryPreparationUseCase` with loaded route patterns and assert `RouteFound`
11. anti-fabrication assertion: route ID is `RKV_C`, not `Jaam`

## CI Test Step

- Updated `.github/workflows/ci.yml`:
  - added explicit `Test` step: `./gradlew test`
  - placed after `Build` and before `Lint`

## Validation Result

Executed:

- `py -3 tools/validate_project_state.py` -> PASS
- `.\gradlew.bat :data-local:test` -> PASS
- `.\gradlew.bat :core-domain:test` -> PASS
- `.\gradlew.bat :feature-search:test` -> PASS
- `.\gradlew.bat build` -> PASS
- `.\gradlew.bat test` -> PASS
- `git diff --check` -> PASS (no diff-check errors)
- `rg -n "MappedGtfsFeed|GtfsFeedParser|GtfsDomainMapper" data-local/src/main` -> no matches

## Files Changed

- `.github/workflows/ci.yml`
- `data-local/build.gradle.kts`
- `data-local/src/main/kotlin/ee/androbus/data/local/dao/FeedSnapshotDao.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/importer/FeedSnapshotImporter.kt`
- `data-local/src/test/kotlin/ee/androbus/data/local/importer/FeedSnapshotImporterTest.kt`
- `data-local/src/test/kotlin/ee/androbus/data/local/integration/RoomFeedImportIntegrationTest.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/GTFS_PIPELINE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/audit/PASS_23_FEED_SNAPSHOT_IMPORTER_AND_CI_TEST.md`

## Scope/Constraint Confirmation

- No UI/Compose/ViewModel changes
- No Hilt/DI or app wiring changes
- No WorkManager/downloader/network/realtime additions
- No Room freshness/hash/version metadata added
- No parser/mapper behavior changes
- No routing/search algorithm changes
- No real `rakvere.zip` committed/used in tests

## Risks / Unknowns

- Room provider remains load-then-serve baseline; runtime app orchestration for import/prepare lifecycle is still missing.
- Provider cache remains city-keyed with active feed selected by latest `prepare(cityId, feedId)` call.
- CI now runs `./gradlew test`, which may increase runtime; no sharding/optimization changes were introduced in this pass.

## Recommended PASS 24

- `PASS 24 — FEED_IMPORT_APP_WIRING_DECISION_DOCS`
