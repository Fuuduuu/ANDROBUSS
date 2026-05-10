# PASS_22B_FEED_CONTRACT_MOVE_AND_ROOM_SCHEMA_WITH_SCOPED_KEYS

## Goal

1. Move `DomainFeedSnapshot` and `DomainFeedSnapshotProvider` from `feature-search` to `core-domain`.
2. Add minimal `data-local` Room baseline with scoped composite keys.
3. Add load-then-serve Room provider baseline.
4. Keep parser/routing/search behavior unchanged.

## Files Changed

### Core-domain / feature-search contract move
- `core-domain/src/main/kotlin/ee/androbus/core/domain/DomainFeedSnapshot.kt` (added)
- `core-domain/src/main/kotlin/ee/androbus/core/domain/DomainFeedSnapshotProvider.kt` (added)
- `feature-search/src/main/kotlin/ee/androbus/feature/search/feed/DomainFeedSnapshot.kt` (deleted)
- `feature-search/src/main/kotlin/ee/androbus/feature/search/feed/DomainFeedSnapshotProvider.kt` (deleted)
- `feature-search/src/main/kotlin/ee/androbus/feature/search/feed/InMemoryDomainFeedSnapshot.kt` (imports only)
- `feature-search/src/test/kotlin/ee/androbus/feature/search/feed/InMemoryDomainFeedSnapshotTest.kt` (imports only)

### Build/dependency
- `data-local/build.gradle.kts` (Room compiler/test setup for this module only)
- `gradle/libs.versions.toml` (Room compiler/testing + KSP + test library aliases)

### data-local Room baseline
- `data-local/src/main/kotlin/ee/androbus/data/local/entity/StopPointEntity.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/entity/RoutePatternEntity.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/entity/PatternStopEntity.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/dao/FeedSnapshotDao.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/database/AppDatabase.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/mapping/FeedEntityMapper.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/provider/RoomDomainFeedSnapshotLoader.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/provider/RoomDomainFeedSnapshotProvider.kt`

### data-local tests
- `data-local/src/test/kotlin/ee/androbus/data/local/mapping/FeedEntityMapperTest.kt`
- `data-local/src/test/kotlin/ee/androbus/data/local/dao/FeedSnapshotDaoTest.kt`
- `data-local/src/test/kotlin/ee/androbus/data/local/provider/RoomDomainFeedSnapshotLoaderTest.kt`

### Docs
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/GTFS_PIPELINE.md`
- `docs/ROUTING_LOGIC.md`
- `docs/TESTING_STRATEGY.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/INVARIANTS.md`
- `docs/audit/PASS_22B_FEED_CONTRACT_MOVE_AND_ROOM_SCHEMA_WITH_SCOPED_KEYS.md`

## Dependency Direction

- `core-domain` now owns feed contract types.
- `feature-search` depends on `core-domain` and keeps only in-memory implementation.
- `data-local` depends on `core-domain` and implements Room baseline/provider.
- No production dependency added between `feature-search` and `data-local`.

## Room Schema

Scoped composite keys:
- stop points: `cityId + feedId + stopId`
- route patterns: `cityId + feedId + patternId`
- pattern stops: `cityId + feedId + patternId + sequence`

Additional behavior:
- `PatternStopEntity.stopId` is not part of the primary key and remains repeatable for loop patterns.
- Composite FK from `pattern_stops` to `route_patterns` uses `CASCADE` delete.

## Scoped Key Decision

- GTFS IDs are treated as feed/city-local for storage.
- Persisted local IDs are reconstructed directly (`stopId -> StopPointId`, `patternId -> RoutePatternId`).
- No ID derivation from names/coordinates/manual text.

## Provider Behavior

- `RoomDomainFeedSnapshotLoader.load(cityId, feedId)` performs suspend DAO reads and returns `DomainFeedSnapshot?`.
- `RoomDomainFeedSnapshotProvider.prepare(cityId, feedId)` loads and caches snapshot for the city.
- `getSnapshot(cityId)` is synchronous cache read only and never touches DAO.

## Tests

- `FeedEntityMapperTest`: scoped-key round-trips, order, duplicate stop IDs, anti-fabrication.
- `FeedSnapshotDaoTest`: scoped queries, same local IDs across scopes, ordered pattern stops, scoped deletes, cascade behavior.
- `RoomDomainFeedSnapshotLoaderTest`: load-then-serve cache behavior, scoped feed selection, preserved order/duplicates, route-search parity, provider contract package check.

## Validation

Executed:
- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :core-domain:test`
- `.\gradlew.bat :feature-search:test`
- `.\gradlew.bat :data-local:test`
- `.\gradlew.bat build`
- `git diff --check`
- `git status --short --untracked-files=all`

Notes:
- Allowed warning observed: `PROJECT_STATE last_accepted_commit may be stale.`
- AGP compileSdk 35 support warning is non-blocking and unchanged.

## Pre-commit audit fixes

- `docs/PROJECT_STATE.yml` invariant references confirmed concrete for PASS 22B:
  - INV-006 points to scoped identity tests in `FeedEntityMapperTest` and `FeedSnapshotDaoTest`.
  - INV-007 now points to load-then-serve behavior tests in `RoomDomainFeedSnapshotLoaderTest` (`getSnapshot` before prepare and cache load after prepare).
- `docs/PROJECT_STATE.yml` required validation commands include `.\gradlew.bat :data-local:test`.
- Reflection guard package check confirmed in `InMemoryDomainFeedSnapshotTest`:
  - `DomainFeedSnapshot` and `DomainFeedSnapshotProvider` imports are from `ee.androbus.core.domain`.
- `rg -n "allowMainThreadQueries" data-local/src` result: no matches.

## Risks

- `RoomDomainFeedSnapshotProvider` cache key is `CityId`; last `prepare(city, feed)` selects active feed for that city.
- Schema baseline excludes freshness/hash/version metadata and import lifecycle policy.
- App runtime wiring/DI for provider preparation remains future work.

## Recommended Next Pass

- `PASS 23 — FEED_SNAPSHOT_IMPORT_OR_APP_WIRING_DECISION`
