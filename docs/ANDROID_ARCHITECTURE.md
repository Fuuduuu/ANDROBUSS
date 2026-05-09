# ANDROID_ARCHITECTURE

Module planning document only. No implementation is present yet.

## Planned Stack

- Kotlin
- Jetpack Compose
- MVVM
- Room
- WorkManager
- Hilt

## Planned Module Boundaries

- `app`: app shell, navigation, dependency graph wiring.
- `core-domain`: canonical models, business rules, stop versus stopPoint semantics.
- `core-gtfs`: GTFS parsing, mapping, and static dataset handling.
- `core-routing`: direct route candidate logic first; transfer logic later.
- `data-local`: Room database, DAOs, offline repositories.
- `data-remote`: feed acquisition, validation, and optional realtime clients.
- `feature-map`, `feature-search`, `feature-stop-board`: user workflows.
- `city-adapters`: per-city rules and feed integration adapters.

## Status

Architecture guidance only, pre-implementation.
