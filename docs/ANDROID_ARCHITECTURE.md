# ANDROID_ARCHITECTURE

Architecture baseline and implementation status snapshot.

## Android Stack Baseline

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Hilt
- Room
- DataStore
- WorkManager
- Coroutines / Flow
- Offline-first data behavior

## Module Structure

- `app`
- `core-domain`
- `core-gtfs`
- `core-routing`
- `data-local`
- `data-remote`
- `feature-map`
- `feature-search`
- `feature-stop-board`
- `feature-route-detail`
- `feature-favourites`
- `feature-alerts`
- `city-adapters`

## Implementation Status (After PASS 31 Accepted)

- Implemented pure Kotlin logic:
  - `core-domain`
  - `core-gtfs`
  - `core-routing`
  - `city-adapters` metadata contract/registry
  - `feature-search` search pipeline core (resolution, enrichment, orchestration, route-query preparation)
  - `core-domain` feed boundary (`DomainFeedSnapshot`, `DomainFeedSnapshotProvider`)
  - `feature-search` in-memory feed provider (`InMemoryDomainFeedSnapshot`)
- Implemented Android baseline:
  - `data-local` scoped Room schema + DAO + mapper + load-then-serve provider baseline.
  - `app` bundled bootstrap baseline with Hilt DI wiring:
    - real static Rakvere JSON snapshot primary asset (internal/MVP baseline)
    - synthetic bundled JSON fallback asset
    - DTO -> `DomainFeedSnapshot` conversion
    - bootstrap lifecycle hardening:
      - check provider cache first
      - if empty, call provider `prepare(cityId, primaryFeedId)` to load Room snapshot
      - import primary real-static snapshot only when Room has no primary-feed snapshot
      - fallback to synthetic asset if primary asset is missing/unreadable
      - after import, call provider `prepare(...)` to populate cache
    - app-owned Hilt modules providing database/dao/importer/loader/provider/bootstrap-loader
    - `AppDatabase.create(context)` remains available as pre-Hilt/test utility
  - `app` presentation baseline (PASS 28A + PASS 28B accepted):
    - `SearchViewModel` exposes `FeedState` and destination input state
    - destination resolution uses feature-search pure Kotlin enrichment pipeline
    - route query state is handled in ViewModel with explicit `searchRoute()` trigger
    - route query requires explicit origin and distinguishes precondition states from `RouteNotFound`
    - route query executes through `DirectRouteQueryPreparationUseCase`
  - `app` Compose baseline (PASS 28C accepted):
    - `MainActivity` uses `@AndroidEntryPoint` and hosts first `SearchScreen`
    - screen uses `hiltViewModel<SearchViewModel>()`
    - destination text is local UI state and is resolved only when user presses "Vali sihtkoht"
    - route query is triggered only by explicit "Otsi" press
    - origin selection is MVP/dev-only hardcoded stop chips (temporary)
    - no navigation graph and no multi-screen setup
  - PASS 31 accepted quick-destination UI layer:
    - `Kiirvalikud` chips are wired in `SearchScreen`
    - chips route through destination query text (`onDestinationChanged(queryText)`)
    - no `StopPointId` shortcuts in quick-chip path
    - quick-chip click does not trigger route search directly
- Skeleton/future:
  - `data-remote`, UI `feature-*` runtime wiring.
- Not implemented yet:
  - Multi-screen Compose/navigation integration consuming prepared snapshot state.
  - origin resolver replacement for dev-only chip selector.
  - dedicated search-screen polish and broader UI composition passes.
  - downloader/cache orchestration and feed refresh lifecycle.
  - public-production downloader/update/freshness lifecycle for freely distributed releases.

## Storage Identity Strategy (PASS 22A)

- Persistence identity for GTFS-mapped IDs is city/feed-scoped.
- Room baseline keys:
  - stop point key: `cityId + feedId + stopId`
  - route pattern key: `cityId + feedId + patternId`
  - pattern stop key: `cityId + feedId + patternId + sequence`
- This is a storage-layer scoping rule and does not change routing behavior.

## Module Responsibilities

- `app`: application shell and composition root.
- `core-domain`: canonical IDs/models/invariants and service calendar semantics.
- `core-gtfs`: minimal GTFS fixture parsing and domain mapping.
- `core-routing`: direct-route candidate search core.
- `data-local`: scoped Room persistence + Room feed snapshot provider baseline.
- `data-remote`: future feed sync/downloader boundary.
- `feature-*`: future UI boundaries.
- `city-adapters`: future city-specific metadata and runtime mapping.

## Dependency Direction Rules

- `core-routing` -> `core-domain`.
- `core-gtfs` -> `core-domain`.
- `data-local` -> `core-domain`.
- `data-remote` -> `core-domain`, `core-gtfs`.
- `city-adapters` -> `core-domain`.
- `feature-search` -> `core-domain`, `core-routing`, `city-adapters`.
- UI feature modules may depend on `core-domain`/`core-routing` as needed.
- `app` orchestrates all Android modules.

Test-only rule:
- `feature-search` tests may depend on `core-gtfs` for fixture integration tests.
- `data-local` tests may depend on `core-gtfs` for parser-fixture integration tests.
- `feature-search` production code must not depend on parser implementation.
- `feature-search` and `data-local` must stay independent in production dependency graph.

## Runtime Wiring Responsibility Matrix (PASS 24 Decision)

| Step | Responsible component | Module layer | Trigger |
| --- | --- | --- | --- |
| Check cache + prepare Room snapshot | `FeedBootstrapLoader` | `app` | Cold start / process restart |
| Read primary static runtime feed asset | `FeedBootstrapLoader` | `app` | Room has no snapshot for primary feed scope |
| Read synthetic fallback feed asset | `FeedBootstrapLoader` | `app` | Primary asset missing/unreadable and Room has no primary snapshot |
| Call `FeedSnapshotImporter.import(...)` | `FeedBootstrapLoader` | `app` | After primary or fallback asset read |
| Call `RoomDomainFeedSnapshotProvider.prepare(...)` | `FeedBootstrapLoader` (startup baseline) | app/feature boundary | During startup bootstrap |
| Use `getSnapshot(cityId)` | `DirectRouteQueryPreparationUseCase` caller | feature-search/app caller | Sync after `prepare(...)` |
| Feed refresh | `data-remote` / WorkManager (future) | `data-remote` | Future background update |

Notes:
- PASS 27 accepted wiring uses app-owned Hilt modules and `@HiltAndroidApp` `AndrobussApplication`.
- `AndrobussApplication.onCreate` is now a protected runtime wiring surface.
- PASS 28A/28B ViewModel baseline exists.
- PASS 28C adds first single-screen Compose wiring in `app`.
- PASS 30 accepted baseline uses real-static primary + synthetic fallback for internal/MVP use.
- No navigation graph / multi-screen wiring is implemented yet.
- No GPS/nearest-stop/network/realtime route-query extensions are implemented.
- No WorkManager/downloader is added yet; public-production freshness remains unresolved.
- `data-local` production code remains parser-agnostic.
- `data-local` tests may use `core-gtfs`, but production must not import parser types.

Forbidden coupling:
- `data-remote` must not directly depend on `city-adapters`.
- `city-adapters` must not directly depend on `data-remote`.
- Feature modules must not parse GTFS directly.

## Pure Kotlin Core Rule

`core-domain`, `core-gtfs`, and `core-routing` must remain Android-free:
- no `android.*` imports,
- no `Context`,
- no lifecycle dependencies.

Violation is a build-breaking architecture error.
