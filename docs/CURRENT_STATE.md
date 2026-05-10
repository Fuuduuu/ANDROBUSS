# CURRENT_STATE

## Repository Baseline

- Expected repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Expected branch: `main`
- Latest accepted HEAD: `709010d` (`PASS 21`)
- Working tree must be clean before a new pass

## Latest Accepted Pass

- `PASS 21 — DOMAIN_FEED_SNAPSHOT_AND_PROVIDER_CONTRACT`

PASS 21 added a parser-agnostic feed boundary in `feature-search`:
- `DomainFeedSnapshot`
- `DomainFeedSnapshotProvider`
- `InMemoryDomainFeedSnapshot`
- integration coverage proving snapshot stop points seed `InMemoryStopPointIndex`
- integration coverage proving snapshot route patterns can be supplied to `DirectRouteQueryPreparationUseCase`

PASS 22A confirms storage-identity strategy for future Room baseline:
- GTFS `stop_id` and `tripId`-derived pattern IDs are treated as feed/city-local identifiers.
- Future storage keys must be city/feed-scoped:
  - stop-point key: `cityId + feedId + stopId`
  - route-pattern key: `cityId + feedId + patternId`
  - pattern-stop key: `cityId + feedId + patternId + sequence`

## Current Core Status

- `core-domain`, `core-gtfs`, `core-routing`, `city-adapters`, and `feature-search` pure Kotlin search stack are implemented and tested.
- `feature-search` has test-scope parser integration only:
  - `testImplementation(project(":core-gtfs"))`
- No production parser dependency from feature-search runtime code.
- `DomainFeedSnapshotProvider` is synchronous and in-memory only in PASS 21.

## Not Implemented Yet

- Room-backed feed snapshot provider
- Room/cache persistence
- Production route-pattern source/provider wiring
- Production feed downloader/refresh flow
- App/ViewModel runtime wiring of the pipeline
- Nearest-stop/geospatial resolution
- UI feature flows

## Current Risks

- Production feed snapshot/provider is not implemented.
- Room/cache is not implemented.
- Production `RoutePattern` source is not implemented.
- UI/ViewModel wiring is not implemented.
- Nearest-stop/geospatial behavior is not implemented.
- `rakvere-smoke` names are synthetic and separate from real Rakvere POI metadata names.

## Current Pass

- `PASS_G01 — GOVERNANCE_BOOTSTRAP_DOCS_ONLY`

## Governance Bootstrap (PASS_G01)

- Added compact governance scaffold docs:
  - `docs/PROJECT_STATE.yml`
  - `docs/INVARIANTS.md`
  - `docs/DEBUG_PLAYBOOK.md`
  - `docs/PASS_TEMPLATE.md`
- No runtime/source/build behavior changes.

## Next Technical Pass

- `PASS 22 — FEED_CONTRACT_MOVE_AND_ROOM_SCHEMA_BASELINE` (with composite storage-key strategy)
