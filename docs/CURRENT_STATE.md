# CURRENT_STATE

## Repository Baseline

- Expected repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Expected branch: `main`
- Latest accepted HEAD: `8cdd748` (`PASS 20B`)
- Working tree must be clean before a new pass

## Latest Accepted Pass

- `PASS 20B — GTFS_PIPELINE_DOCS_AND_DIAGRAMS_SYNC`

PASS 21 adds a parser-agnostic feed boundary in `feature-search`:
- `DomainFeedSnapshot`
- `DomainFeedSnapshotProvider`
- `InMemoryDomainFeedSnapshot`
- integration coverage proving snapshot stop points seed `InMemoryStopPointIndex`
- integration coverage proving snapshot route patterns can be supplied to `DirectRouteQueryPreparationUseCase`

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

- `PASS 21 — DOMAIN_FEED_SNAPSHOT_AND_PROVIDER_CONTRACT`

## Next Technical Pass

- `PASS 22 — DATA_LOCAL_ROOM_SCHEMA_AND_FEED_SNAPSHOT_PROVIDER`
