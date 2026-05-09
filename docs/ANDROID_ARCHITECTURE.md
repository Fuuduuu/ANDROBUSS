# ANDROID_ARCHITECTURE

Architecture baseline for implementation planning. This document is docs-only and pre-code.

## Status

- PASS 01 architecture baseline: locked for PASS 02 preparation.
- No Android source modules created yet.
- No Gradle project files created yet.

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

## Planned Module Structure

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

## Module Responsibilities

- `app`: Android app shell, navigation graph, DI composition root, app-level lifecycle wiring.
- `core-domain`: canonical entities and value objects, domain invariants, `Stop` vs `StopPoint` semantics, shared use-case contracts.
- `core-gtfs`: GTFS parsing pipeline, validation, mapping from raw feed rows to domain-facing structures.
- `core-routing`: route candidate search and ranking interfaces, direct-route engine first, transfer expansion later.
- `data-local`: Room schema, DAO interfaces, local repositories, cache persistence policy for offline-first.
- `data-remote`: feed download orchestration, integrity checks, sync inputs, optional realtime acquisition surfaces.
- `feature-map`: map-first destination assist UI and interactions.
- `feature-search`: destination-first stop/area search UI and query orchestration.
- `feature-stop-board`: departure board UI for a selected stop/stopPoint context.
- `feature-route-detail`: selected route card detail UI and rider guidance screens.
- `feature-favourites`: saved stops/routes/preferences UI and use-case integration.
- `feature-alerts`: user-facing service alert UI surfaces backed by adapter-capable data.
- `city-adapters`: per-city adapter contracts and implementations for source quirks, optional realtime mapping, and local data policy differences.

## Allowed Dependency Direction

Dependency flow is inward toward stable core contracts:

1. `app` can depend on `feature-*`, `data-local`, `data-remote`, `core-*`, and `city-adapters`.
2. `feature-*` can depend on `core-domain`, `core-routing`, and app-level interfaces exposed through DI.
3. `data-local` can depend on `core-domain` and `core-gtfs`.
4. `data-remote` can depend on `core-domain` and `core-gtfs`.
5. `city-adapters` can depend on `core-domain` and `core-gtfs`; never on feature modules.
6. `core-routing` can depend on `core-domain`.
7. `core-gtfs` can depend on `core-domain` for canonical mapping targets.
8. `core-domain` depends on nothing outside standard Kotlin primitives/libraries.
9. `data-remote` and `city-adapters` are orchestrated by app/sync composition and must not directly depend on each other.

## Forbidden Dependencies and Rules

- `core-domain` must not depend on Android framework classes.
- `core-gtfs` must not depend on Android framework classes.
- `core-routing` must not depend on Android framework classes.
- Feature modules must not parse GTFS directly.
- UI layers must not hardcode city-specific data rules.
- Only `city-adapters` may own city-specific rule branching.
- Only data/core layers may transform raw feed formats into canonical structures.
- `data-remote` must not directly depend on `city-adapters`.
- `city-adapters` must not directly depend on `data-remote`.

## Pure Kotlin Core Rule

`core-domain`, `core-gtfs`, and `core-routing` must contain zero Android framework imports.

- No `android.*` imports.
- No `Context`.
- No lifecycle dependencies.
- Violation is a build-breaking architecture error.

KMP compatibility note: these modules should stay Kotlin/JVM/KMP-compatible where practical.

## Implementation Guardrails

- Preserve strict `Stop` vs `StopPoint` semantics across all module interfaces.
- Keep `ServiceCalendarResolver` behavior isolated, test-heavy, and adapter-agnostic.
- Keep realtime optional; static GTFS remains canonical base for availability decisions.
