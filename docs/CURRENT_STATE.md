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

## Architecture Audit Artifacts

- `docs/audit/standalone-bus-app-architecture.md`
- `docs/audit/PASS_01B_AUDIT.md`
- `docs/audit/PASS_02_AUDIT.md`
- `docs/audit/PASS_03_GTFS_SOURCE_DISCOVERY.md`

## Current Pass

`PASS 03 — GTFS_SOURCE_DISCOVERY`

## PASS 03 Discovery Snapshot

- Official authority remains Regionaal- ja Põllumajandusministeerium / Ühistranspordiregistri avaandmed.
- Legacy `peatus.ee/gtfs/gtfs.zip` path did not behave as a live ZIP source in this pass.
- Live downloadable ZIP feeds were verified on `eu-gtfs.remix.com` object URLs.
- Unified and split feeds both exist; city mapping must be feed-aware.

## Guardrails Still Active

- No GTFS parser implementation.
- No Room schema/entities/DAOs.
- No feature UI screens or ViewModels.
- No Map SDK dependency.
- No backend assumptions.

## Next Likely Pass

`PASS 04 — GTFS_FIXTURE_STRATEGY_AND_CITY_MAPPING`
