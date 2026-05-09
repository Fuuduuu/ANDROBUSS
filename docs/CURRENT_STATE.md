# CURRENT_STATE

## Repository State

- Repository initialized for ANDROBUSS planning work.
- Gradle skeleton and Android module structure exist (from PASS 02).
- No product logic implementation exists yet.

## Documentation State

- PASS 00 docs bootstrap completed.
- PASS 01 architecture baseline completed.
- PASS 01B architecture review fixes completed.
- PASS 02 skeleton and build setup completed.
- PASS 03 GTFS source discovery completed (docs/audit only).
- PASS 04 GTFS fixture strategy and city mapping completed (docs/audit only).

## Architecture Audit Artifacts

- `docs/audit/standalone-bus-app-architecture.md`
- `docs/audit/PASS_01B_AUDIT.md`
- `docs/audit/PASS_02_AUDIT.md`
- `docs/audit/PASS_03_GTFS_SOURCE_DISCOVERY.md`
- `docs/audit/PASS_04_GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING.md`

## Current Pass

`PASS 04 — GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING`

## PASS 04 Planning Snapshot

- City/feed mapping baseline is defined for Wave 0, Wave 1, Wave 2, later targets, and future-only Tallinn/Tartu.
- Conceptual `CityFeedMapping` metadata shape is defined in docs.
- Fixture strategy is defined for smoke, stop-point precision, direct-route, calendar, and city-mapping tests.
- Parser preconditions are defined, but parser implementation is not started.

## Guardrails Still Active

- No GTFS parser implementation.
- No Room schema/entities/DAOs.
- No feature UI screens or ViewModels.
- No Map SDK dependency.
- No backend assumptions.

## Next Likely Pass

`PASS 05 — CORE_DOMAIN_STOP_AND_PATTERN_MODELS`
