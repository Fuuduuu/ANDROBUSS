# PASS_25_BUNDLED_FEED_BOOTSTRAP_SERIALIZATION_AND_APP_LAYER

## Objective

Implement the minimal app-layer bundled feed bootstrap path decided in PASS 24:
- bundled synthetic JSON asset in app,
- app DTO serialization + domain mapping,
- bootstrap loader calling `FeedSnapshotImporter.import(...)` and `RoomDomainFeedSnapshotProvider.prepare(...)`,
- pre-Hilt `AndrobussApplication` startup wiring,
- executable app bootstrap tests.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at gate: `fdf4668`
- Working tree: clean before PASS 25 edits
- `tools/validate_project_state.py`: passed (allowed warning-only stale commit note)

## Files Read

- `README.md`
- `AGENTS.md`
- `docs/PROJECT_STATE.yml`
- `docs/INVARIANTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/GTFS_PIPELINE.md`
- `docs/ROADMAP.md`
- `docs/TESTING_STRATEGY.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/audit/PASS_23_FEED_SNAPSHOT_IMPORTER_AND_CI_TEST.md`
- `docs/audit/PASS_24_FEED_BOOTSTRAP_AND_RUNTIME_WIRING_DECISION.md`
- `gradle/libs.versions.toml`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/**`
- `data-local/src/main/kotlin/ee/androbus/data/local/database/AppDatabase.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/importer/FeedSnapshotImporter.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/provider/RoomDomainFeedSnapshotLoader.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/provider/RoomDomainFeedSnapshotProvider.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/DomainFeedSnapshot.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/StopPoint.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/RoutePattern.kt`

## Serialization Dependency Added

- Added Kotlin serialization plugin alias and JSON library alias in version catalog.
- Root plugin management now applies Kotlin serialization plugin alias `apply false`.
- App module applies serialization plugin and depends on JSON runtime.
- Runtime compatibility fix: serialization runtime pinned to `1.6.3` for Kotlin `1.9.24` compatibility.

## DTO Layer Added

- Added `app/bootstrap/BootstrapFeedDto.kt` with app-only `@Serializable` DTOs:
  - `BootstrapFeedDto`
  - `StopPointDto`
  - `RoutePatternDto`
- Added mapping extension:
  - `BootstrapFeedDto.toDomainFeedSnapshot()`
- Mapping invariants:
  - `StopPointId` from DTO `id` only.
  - `RoutePatternId` from DTO `id` only.
  - stop order preserved.
  - duplicate stop IDs preserved.
  - no core-domain serialization annotations.

## Bundled Asset Added

- Added synthetic asset:
  - `app/src/main/assets/bootstrap/rakvere_bootstrap.json`
- Includes explicit IDs:
  - `RKV_A_OUT`, `RKV_A_IN`, `RKV_B`, `RKV_C`
- Includes route patterns:
  - `pattern:T1`: `RKV_A_OUT -> RKV_B -> RKV_C`
  - `pattern:T3`: `RKV_A_OUT -> RKV_B -> RKV_A_OUT`
- Asset is synthetic bootstrap data; not real `rakvere.zip`.

## FeedBootstrapLoader Behavior

- Added `app/bootstrap/FeedBootstrapLoader.kt`.
- `bootstrapIfNeeded()` behavior:
  - returns early when provider cache already has snapshot,
  - reads/decodes bundled asset,
  - converts DTO -> `DomainFeedSnapshot`,
  - imports via `FeedSnapshotImporter`,
  - prepares provider via `RoomDomainFeedSnapshotProvider.prepare(cityId, feedId)`.
- Missing/invalid asset handling:
  - returns `null` DTO and exits safely (FeedNotReady-style no crash).
- Added optional `assetPath` constructor parameter for missing-asset test.

## AndrobussApplication Wiring

- Added `app/AndrobussApplication.kt`.
- Startup actions (pre-Hilt baseline):
  - create DB with `AppDatabase.create(context)`,
  - construct DAO/importer/loader/provider,
  - launch background bootstrap coroutine and call `bootstrapIfNeeded()`.
- `AndroidManifest.xml` updated with `android:name=".AndrobussApplication"`.

## AppDatabase Factory

- Added temporary pre-Hilt factory in `AppDatabase`:
  - `AppDatabase.create(context)` using `Room.databaseBuilder(...)`.
- No schema/version/DAO/entity changes.
- No `allowMainThreadQueries()` added.

## Tests Added

- `BootstrapFeedDtoTest`:
  - city/count mapping,
  - anti-fabrication (`id` vs `displayName`),
  - route stop order,
  - duplicate stop IDs.
- `FeedBootstrapLoaderTest` (Robolectric):
  - bootstrap loads snapshot,
  - anti-fabrication ID assertions,
  - idempotent repeated bootstrap,
  - missing-asset safe behavior.

## Anti-Fabrication Checks

- DTO and bootstrap tests assert IDs are explicit `RKV_*` IDs.
- Tests assert no `StopPointId("Jaam")` appears in loaded snapshot IDs.
- No ID derivation from display names or coordinates.

## Protected Surface Note

- `AndrobussApplication.onCreate` bootstrap wiring is now an active runtime lifecycle surface.
- This remains pre-Hilt and is intentionally temporary until dedicated DI/lifecycle pass.

## Validation Result

Commands executed:
- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :app:test`
- `.\gradlew.bat :data-local:test`
- `.\gradlew.bat :core-domain:test`
- `.\gradlew.bat :feature-search:test`
- `.\gradlew.bat build`
- `.\gradlew.bat test`
- `git diff --check`
- `git status --short --untracked-files=all`
- `rg -n "@Serializable" core-domain/src`
- `rg -n "allowMainThreadQueries" data-local/src`
- `rg -n "GtfsFeedParser|GtfsDomainMapper|MappedGtfsFeed" app/src/main data-local/src/main`

Expected outcomes were confirmed:
- no `@Serializable` in `core-domain/src`,
- no `allowMainThreadQueries` in `data-local/src`,
- no parser types in `app/src/main` or `data-local/src/main`.

## Files Changed

- `gradle/libs.versions.toml`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/kotlin/ee/androbus/app/AndrobussApplication.kt`
- `app/src/main/kotlin/ee/androbus/app/bootstrap/BootstrapFeedDto.kt`
- `app/src/main/kotlin/ee/androbus/app/bootstrap/FeedBootstrapLoader.kt`
- `app/src/main/assets/bootstrap/rakvere_bootstrap.json`
- `app/src/test/kotlin/ee/androbus/app/bootstrap/BootstrapFeedDtoTest.kt`
- `app/src/test/kotlin/ee/androbus/app/bootstrap/FeedBootstrapLoaderTest.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/database/AppDatabase.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/ROADMAP.md`
- `docs/GTFS_PIPELINE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/audit/PASS_25_BUNDLED_FEED_BOOTSTRAP_SERIALIZATION_AND_APP_LAYER.md`

## Explicit Non-Changes Confirmation

- No Hilt/DI implementation.
- No ViewModel/Compose/UI implementation.
- No WorkManager/downloader/network/realtime implementation.
- No nearest-stop/geospatial implementation.
- No Room schema/entity/DAO behavioral changes.
- No `core-domain` `@Serializable` annotations.
- No parser invocation in app production code.
- No real `rakvere.zip` committed.
- No freshness/hash/version metadata additions.

## Risks / Unknowns

- App bootstrap wiring is pre-Hilt and currently anchored in `Application.onCreate`.
- Bundled bootstrap asset is synthetic; real Rakvere asset generation and lifecycle policy remain future.
- Provider cache remains city-keyed with active feed selected by last `prepare(cityId, feedId)`.

## Recommended PASS 26

- `PASS 26 — REAL_RAKVERE_FEED_ASSET_OR_HILT_BOOTSTRAP_DECISION`
  - choose next priority between:
    - real bundled feed asset pipeline, or
    - Hilt/bootstrap lifecycle ownership hardening.