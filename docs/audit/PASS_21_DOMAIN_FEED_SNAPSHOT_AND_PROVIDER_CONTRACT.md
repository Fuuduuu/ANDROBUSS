# PASS_21_DOMAIN_FEED_SNAPSHOT_AND_PROVIDER_CONTRACT

## Objective

Define a small parser-agnostic feed snapshot/provider boundary in `feature-search` that can supply `StopPoint` and `RoutePattern` domain data to existing search pipeline components, without Room/data-local or parser/runtime coupling changes.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- `git status --short --untracked-files=all`: clean at pass start
- `git branch --show-current`: `main`
- `git remote -v`: `origin https://github.com/Fuuduuu/ANDROBUSS.git` (fetch/push)
- `git rev-parse HEAD`: `8cdd748fcd3fa616cefddc193bbcade56f0e3e47`
- `git log --oneline -12`: includes `8cdd748 docs: sync GTFS pipeline docs and diagrams`

## Files Read

- `README.md`
- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/ROADMAP.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/UX_PRINCIPLES.md`
- `docs/TESTING_STRATEGY.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/audit/PASS_20_GTFS_FIXTURE_TO_SEARCH_PIPELINE_INTEGRATION_TEST.md`
- `docs/audit/PASS_20B_GTFS_PIPELINE_DOCS_AND_DIAGRAMS_SYNC.md`
- `feature-search/build.gradle.kts`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/resolution/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/orchestration/**`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/bridge/**`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/integration/**`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/**`
- `data-local/build.gradle.kts`
- module `build.gradle.kts` files for dependency-scope confirmation

## Production Feed Contract Added

Added `feature-search` parser-agnostic feed boundary in package:
- `ee.androbus.feature.search.feed`

Files:
- `DomainFeedSnapshot.kt`
- `DomainFeedSnapshotProvider.kt`
- `InMemoryDomainFeedSnapshot.kt`

## Why Owner Is feature-search/feed

- Boundary is consumed by `feature-search` search pipeline layers.
- Keeps parser/Room concerns out of orchestration and resolution classes.
- Avoids premature coupling to `data-local` before Room contract pass.

## Parser-Agnostic Boundary

- `DomainFeedSnapshot` stores only domain types:
  - `CityId`
  - `List<StopPoint>`
  - `List<RoutePattern>`
- No `core-gtfs` parser types are referenced.
- No Room entities, DAO types, or DB concerns are referenced.

## Provider Behavior

- `DomainFeedSnapshotProvider` is synchronous (`fun getSnapshot(cityId): DomainFeedSnapshot?`) in PASS 21.
- `InMemoryDomainFeedSnapshot` is a pure Kotlin single-city implementation:
  - returns the snapshot when `cityId` matches
  - returns `null` when `cityId` differs

## Snapshot Semantics

- `stopPoints` and `routePatterns` are carried together in one snapshot to reduce feed-version mismatch risk.
- PASS 21 intentionally does not add freshness/hash/timestamp metadata.
- PASS 21 intentionally does not add async/suspend/Flow behavior.

## Tests Added

Added:
- `feature-search/src/test/kotlin/ee/androbus/feature/search/feed/InMemoryDomainFeedSnapshotTest.kt`

Coverage includes:
1. `getSnapshot` returns snapshot for correct city.
2. `getSnapshot` returns `null` for different city.
3. Snapshot exposes `stopPoints` unchanged.
4. Snapshot exposes `routePatterns` unchanged.
5. Snapshot `stopPoints` seed `InMemoryStopPointIndex` (`Jaam` -> `RKV_C`).
6. Snapshot `routePatterns` are consumed by `DirectRouteQueryPreparationUseCase` (`Executed(RouteFound)`).
7. Android-free reflection guard for:
   - `DomainFeedSnapshot`
   - `DomainFeedSnapshotProvider`
   - `InMemoryDomainFeedSnapshot`

## Validation Result

- `.\gradlew.bat :feature-search:test` -> PASS
- `.\gradlew.bat :feature-search:build` -> PASS
- `.\gradlew.bat build` -> PASS
- `git diff --check` -> PASS (line-ending warnings only)
- `git status --short --untracked-files=all` -> PASS 21 scoped files only

## Files Changed

- `feature-search/src/main/kotlin/ee/androbus/feature/search/feed/DomainFeedSnapshot.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/feed/DomainFeedSnapshotProvider.kt`
- `feature-search/src/main/kotlin/ee/androbus/feature/search/feed/InMemoryDomainFeedSnapshot.kt`
- `feature-search/src/test/kotlin/ee/androbus/feature/search/feed/InMemoryDomainFeedSnapshotTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/TRUTH_INDEX.md`
- `docs/ROUTING_LOGIC.md`
- `docs/GTFS_PIPELINE.md`
- `docs/TESTING_STRATEGY.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/audit/PASS_21_DOMAIN_FEED_SNAPSHOT_AND_PROVIDER_CONTRACT.md`

## No Room/UI/Network/Parser/Routing-Change Confirmation

- No Room/DAO/AppDatabase changes.
- No `data-local` or `data-remote` source changes.
- No UI/Compose/ViewModel changes.
- No network downloader/realtime/cache changes.
- No `core-gtfs` parser/mapper behavior changes.
- No `core-routing` or `DirectRouteSearch` behavior/signature changes.
- No `DirectRouteQueryBridge`, `DirectRouteQueryPreparationUseCase`, `StopCandidateEnricher`, or `InMemoryStopPointIndex` behavior/signature changes.

## Risks / Unknowns

- Provider is still in-memory only; Room-backed implementation remains future work.
- No freshness/version metadata exists yet, so snapshot lifecycle policy is still external.
- Runtime wiring into app/ViewModel flow remains pending.

## Recommended PASS 22

`PASS 22 — DATA_LOCAL_ROOM_SCHEMA_AND_FEED_SNAPSHOT_PROVIDER`
