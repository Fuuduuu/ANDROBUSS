# PASS_FEED_02_ROOM_SCHEMA_EXPORT_AND_FEED_METADATA_ENTITY

## Scope

Narrow `data-local` foundation pass:
- Room schema export enablement
- `FeedMetadataEntity` + `FeedMetadataDao`
- explicit `MIGRATION_1_2`
- DAO and migration tests
- minimal governance/docs sync

## Claude Audit Input Summary

- `AppDatabase` was `version = 1` with `exportSchema = false`.
- No schema export location was configured.
- `FeedMetadataEntity` / `FeedMetadataDao` / migration layer were missing.
- Downloader, WorkManager, data-remote implementation, and `DomainFeedSnapshot` metadata were explicitly out of scope.

## Schema Export Issue And Resolution

- Initial state: no `data-local/schemas` JSON files existed.
- Added KSP Room schema export argument:
  - `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`
- Enabled `exportSchema = true` in `AppDatabase`.
- Generated Room schema JSON for v1 first (before version bump), then continued to v2.
- Added unit-test assets mapping so `MigrationTestHelper` can read schema JSON from local unit tests.

## Implementation Summary

### Data-local changes

- Added `FeedMetadataEntity`:
  - `cityId`, `feedId`, `downloadedAt`, `sourceUrl`, `feedVersion`, `isActive`
  - composite PK (`cityId`, `feedId`)
  - index (`cityId`, `isActive`)
- Added `FeedMetadataDao`:
  - `upsert`
  - `getByScope`
  - `getActiveFeed`
  - `activateFeed` transactional activation
  - `deactivateAllForCity`
  - `markActive`
- Added `MIGRATION_1_2`:
  - creates `feed_metadata`
  - creates `index_feed_metadata_cityId_isActive`
- Updated `AppDatabase`:
  - Room version `1 -> 2`
  - `exportSchema = true`
  - includes `FeedMetadataEntity`
  - exposes `feedMetadataDao()`
  - registers `MIGRATION_1_2` in `create(context)`

### Tests

- Added `FeedMetadataDaoTest` coverage:
  - upsert/get
  - no-active behavior
  - activation behavior
  - deactivate previous active feed per city
  - missing-target activation preserves current active feed
  - city isolation for activation
- Added `AppDatabaseMigrationTest` using `MigrationTestHelper`:
  - verifies migration `1 -> 2`
  - validates migrated DB can read/write `feed_metadata`

## Files Changed

- `data-local/build.gradle.kts`
- `data-local/src/main/kotlin/ee/androbus/data/local/database/AppDatabase.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/database/Migrations.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/entity/FeedMetadataEntity.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/dao/FeedMetadataDao.kt`
- `data-local/src/test/kotlin/ee/androbus/data/local/dao/FeedMetadataDaoTest.kt`
- `data-local/src/test/kotlin/ee/androbus/data/local/database/AppDatabaseMigrationTest.kt`
- `data-local/schemas/ee.androbus.data.local.database.AppDatabase/1.json`
- `data-local/schemas/ee.androbus.data.local.database.AppDatabase/2.json`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/audit/PASS_FEED_02_ROOM_SCHEMA_EXPORT_AND_FEED_METADATA_ENTITY.md`

## Validation Summary

- `py -3 tools/validate_project_state.py` passed (stale accepted-commit warning tolerated)
- `./gradlew.bat :data-local:test` passed
- `./gradlew.bat :app:test` passed
- `./gradlew.bat detekt` passed
- `./gradlew.bat build` passed
- boundary greps run; forbidden scope remained closed

## Forbidden Scope Respected

- No downloader implementation
- No WorkManager implementation
- No data-remote implementation changes
- No app bootstrap behavior changes
- No `DomainFeedSnapshot` metadata changes
- No changes in `feature-search`, `core-domain`, `core-gtfs`, `core-routing`, `city-adapters`
- No raw GTFS ZIP committed

## Non-goals (explicit)

- no downloader
- no WorkManager
- no `DomainFeedSnapshot` metadata
- no app bootstrap change
- no `FeedSnapshotImporter` / provider behavior change

## Next Recommended Pass

- `PASS_FEED_03_MANUAL_FEED_DOWNLOADER_AND_IMPORT_PIPELINE`
