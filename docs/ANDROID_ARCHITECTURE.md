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

## Implementation Status (After PASS 22B)

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
- Skeleton/future:
  - `app`, `data-remote`, UI `feature-*` runtime wiring.
- Not implemented yet:
  - app runtime wiring that calls Room provider `prepare(...)` before search flows.
  - downloader/cache orchestration and feed refresh lifecycle.
  - Compose feature flows and ViewModel wiring beyond minimal shell.

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
- `data-local` -> `core-domain`, `core-gtfs`.
- `data-remote` -> `core-domain`, `core-gtfs`.
- `city-adapters` -> `core-domain`.
- `feature-search` -> `core-domain`, `core-routing`, `city-adapters`.
- UI feature modules may depend on `core-domain`/`core-routing` as needed.
- `app` orchestrates all Android modules.

Test-only rule:
- `feature-search` tests may depend on `core-gtfs` for fixture integration tests.
- `feature-search` production code must not depend on parser implementation.
- `feature-search` and `data-local` must stay independent in production dependency graph.

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
