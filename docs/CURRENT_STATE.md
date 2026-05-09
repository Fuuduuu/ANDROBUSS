# CURRENT_STATE

## Repository State

- Repository initialized for ANDROBUSS planning work.
- Gradle skeleton and Android module structure exist (from PASS 02).
- Core-domain baseline models now exist (PASS 05).
- No parser, routing engine, Room schema, or Android UI logic implementation exists yet.

## Documentation State

- PASS 00 docs bootstrap completed.
- PASS 01 architecture baseline completed.
- PASS 01B architecture review fixes completed.
- PASS 02 skeleton and build setup completed.
- PASS 03 GTFS source discovery completed (docs/audit only).
- PASS 04 GTFS fixture strategy and city mapping completed (docs/audit only).
- PASS 05 core-domain stop/pattern model pass completed.

## Architecture Audit Artifacts

- `docs/audit/standalone-bus-app-architecture.md`
- `docs/audit/PASS_01B_AUDIT.md`
- `docs/audit/PASS_02_AUDIT.md`
- `docs/audit/PASS_03_GTFS_SOURCE_DISCOVERY.md`
- `docs/audit/PASS_04_GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING.md`
- `docs/audit/PASS_05_CORE_DOMAIN_STOP_AND_PATTERN_MODELS.md`

## Current Pass

`PASS 05 — CORE_DOMAIN_STOP_AND_PATTERN_MODELS`

## PASS 05 Snapshot

- Canonical pure Kotlin IDs and models were added in `core-domain`.
- StopGroup vs StopPoint distinction is now explicit in code.
- RoutePattern ordering and minimum-stop invariants are now enforced.
- `DataConfidence` enum includes `STATIC`, `FORECAST`, `REALTIME`.
- No Android dependency was introduced in core-domain.

## Guardrails Still Active

- No GTFS parser implementation.
- No service calendar resolver implementation.
- No Room schema/entities/DAOs.
- No routing engine implementation.
- No feature UI screens or ViewModels.
- No Map SDK dependency.

## Next Likely Pass

`PASS 06 — SERVICE_CALENDAR_RESOLVER_SPEC_AND_TESTS`
