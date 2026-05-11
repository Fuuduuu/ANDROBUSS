# PASS_24_FEED_BOOTSTRAP_AND_RUNTIME_WIRING_DECISION

## Objective

Document the MVP runtime feed bootstrap lifecycle and wiring responsibilities before adding any app/runtime implementation code.

## Repo guard result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- `git status --short --untracked-files=all`: clean before edits
- `git branch --show-current`: `main`
- `git remote -v`: `https://github.com/Fuuduuu/ANDROBUSS.git` (fetch/push)
- `git rev-parse HEAD`: `0d09b36ba9dc21ba2d5e79fd5b7f5af0bfb26f23`
- `git log --oneline -12`: includes `0d09b36 feat(data-local): add feed snapshot importer`
- `py -3 tools/validate_project_state.py`: passed

## Files read

- `README.md`
- `AGENTS.md`
- `docs/PROJECT_STATE.yml`
- `docs/INVARIANTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/ROADMAP.md`
- `docs/GTFS_PIPELINE.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/ROUTING_LOGIC.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_22B_FEED_CONTRACT_MOVE_AND_ROOM_SCHEMA_WITH_SCOPED_KEYS.md`
- `docs/audit/PASS_23_FEED_SNAPSHOT_IMPORTER_AND_CI_TEST.md`
- `data-local/src/main/kotlin/ee/androbus/data/local/importer/FeedSnapshotImporter.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/provider/RoomDomainFeedSnapshotProvider.kt`
- `data-local/src/main/kotlin/ee/androbus/data/local/provider/RoomDomainFeedSnapshotLoader.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/**` (inspected current file set)
- `.github/workflows/ci.yml`

## Decisions recorded

- PASS 24 is docs-only.
- MVP bootstrap feed source decision: bundled APK asset, not live network download.
- Runtime bootstrap responsibilities documented for app-layer future owner and provider prepare caller.
- `FeedNotReady` documented as valid first-launch state, not an error.
- Active feed policy documented for MVP: last prepared feed per city.
- No runtime implementation added in this pass.

## Bundled asset strategy

- First launch / Room empty flow:
  1. app layer reads bundled feed asset
  2. app layer converts asset to `DomainFeedSnapshot`
  3. app layer calls `FeedSnapshotImporter.import(cityId, feedId, snapshot)`
  4. app/search bootstrap owner calls `RoomDomainFeedSnapshotProvider.prepare(cityId, feedId)`
  5. callers use synchronous cache-only `getSnapshot(cityId)` after prepare
- Serialization format choice (JSON/protobuf/other) is deferred to PASS 25.
- Real `rakvere.zip` is not committed and not used directly as a bundled app asset.

## Responsibility matrix

Documented in `docs/ANDROID_ARCHITECTURE.md`:

- bundled asset read owner (future app bootstrap component)
- importer call owner (same bootstrap component)
- provider prepare call owner (future search bootstrap / ViewModel boundary)
- snapshot consumption timing (`getSnapshot` only after prepare)
- future downloader/WorkManager owner (`data-remote`)

## Active feed policy

- MVP policy: active feed for a city is the last `prepare(cityId, feedId)` call for that city.
- No multi-feed runtime selection UX is implemented in PASS 24.

## FeedNotReady state

- Empty Room / unprepared provider is a valid startup state named `FeedNotReady`.
- `FeedNotReady` must not be treated as a route-not-found or empty-search-result outcome.

## No-code confirmation

- No Kotlin/source files changed.
- No tests changed.
- No Gradle/build files changed.
- No CI workflow files changed.
- No Room schema/DAO/database/mapper/provider code changed.
- No app code / Hilt / ViewModel / Compose / WorkManager / downloader changes.

## Validation result

Executed:

- `py -3 tools/validate_project_state.py` -> PASS
- `git diff --check` -> PASS
- `git status --short --untracked-files=all` -> only PASS 24 docs files changed

## Files changed

- `docs/GTFS_PIPELINE.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/ROADMAP.md`
- `docs/CURRENT_STATE.md`
- `docs/PROJECT_STATE.yml`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/audit/PASS_24_FEED_BOOTSTRAP_AND_RUNTIME_WIRING_DECISION.md`

## Risks / unknowns

- Bundled asset serialization format is not chosen yet.
- App-layer bootstrap orchestration is not implemented yet.
- Hilt/DI and ViewModel ownership are deferred until bootstrap baseline exists.
- Downloader/WorkManager refresh path is deferred.
- Freshness/hash/version metadata is deferred.

## Recommended PASS 25

- `PASS 25 — BUNDLED_FEED_BOOTSTRAP_APP_LAYER`
