# CURRENT_STATE

## Repository Baseline

- Expected repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Expected branch: `main`
- Latest accepted HEAD: `1ae1daa` (`PASS 20`)
- Working tree must be clean before a new pass

## Latest Accepted Pass

- `PASS 20 — GTFS_FIXTURE_TO_SEARCH_PIPELINE_INTEGRATION_TEST`

PASS 20 proved fixture-level parser-to-search integration:
- `GtfsFeedParser` -> `GtfsDomainMapper` -> `MappedGtfsFeed.stopPoints` -> `InMemoryStopPointIndex`
- `MappedGtfsFeed.routePatterns` -> `DirectRouteQueryPreparationUseCase`
- same-name `Keskpeatus` remains two distinct `StopPointId` values
- no production runtime wiring changes

## Current Core Status

- `core-domain`, `core-gtfs`, `core-routing`, `city-adapters`, and `feature-search` pure Kotlin search stack are implemented and tested.
- `feature-search` has test-scope parser integration only:
  - `testImplementation(project(":core-gtfs"))`
- No production parser dependency from feature-search runtime code.

## Not Implemented Yet

- Production feed snapshot/provider boundary
- Room/cache persistence
- Production route-pattern source/provider
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

- `PASS 20B — GTFS_PIPELINE_DOCS_AND_DIAGRAMS_SYNC` (docs-only)

## Next Technical Pass

- `PASS 21 — FEED_DOMAIN_SNAPSHOT_AND_ROUTE_PATTERN_PROVIDER_SPEC`
