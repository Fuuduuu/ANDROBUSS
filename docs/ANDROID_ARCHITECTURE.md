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

## Implementation Status (After PASS 08)

- Implemented pure Kotlin logic:
  - `core-domain`
  - `core-gtfs`
  - `core-routing`
- Skeleton/future:
  - `app`, `data-local`, `data-remote`, `feature-*`, `city-adapters` runtime logic.
- Not implemented yet:
  - Room schema and cache layer.
  - Hilt graph integration.
  - Compose feature flows beyond minimal shell.

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
- `city-adapters` -> `core-domain`, `core-gtfs`.
- `feature-map` and `feature-route-detail` -> `core-routing`.
- `feature-search`, `feature-stop-board`, `feature-favourites`, `feature-alerts` -> `core-domain`.
- `app` orchestrates all Android modules.

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
