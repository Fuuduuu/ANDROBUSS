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

## Implementation Status (After PASS 21)

- Implemented pure Kotlin logic:
  - `core-domain`
  - `core-gtfs`
  - `core-routing`
  - `city-adapters` metadata contract/registry
  - `feature-search` search pipeline core (resolution, enrichment, orchestration, route-query preparation)
  - `feature-search` parser-agnostic feed boundary (`DomainFeedSnapshot`, `DomainFeedSnapshotProvider`, `InMemoryDomainFeedSnapshot`)
- Skeleton/future:
  - `app`, `data-local`, `data-remote`, UI `feature-*` runtime wiring.
- Not implemented yet:
  - Room schema and cache layer.
  - Room-backed feed provider implementation and downloader/cache orchestration.
  - Compose feature flows and ViewModel wiring beyond minimal shell.

## Module Responsibilities

- `app`: application shell and composition root.
- `core-domain`: canonical IDs/models/invariants and service calendar semantics.
- `core-gtfs`: minimal GTFS fixture parsing and domain mapping.
- `core-routing`: direct-route candidate search core.
- `data-local`: future Room persistence boundary.
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
- `data-local` and Room-backed provider wiring remain PASS 22 scope and are not yet implemented.

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
