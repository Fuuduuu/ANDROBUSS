# GTFS_PIPELINE

GTFS pipeline status after PASS 20.

## Current Implemented Scope

- `core-gtfs` can parse tiny local fixture folders.
- Supported file set in current parser:
  - `agency.txt`
  - `stops.txt`
  - `routes.txt`
  - `trips.txt`
  - `stop_times.txt`
  - `calendar.txt` and/or `calendar_dates.txt`
- Required-file validation exists.
- Calendar exception mapping exists:
  - `exception_type 1` -> `ADD_SERVICE`
  - `exception_type 2` -> `REMOVE_SERVICE`
- Domain mapping exists for:
  - stop points,
  - route patterns,
  - trips,
  - service calendars/exceptions.

## Fixture Status

- Parser tests use synthetic tiny fixture only:
  - `core-gtfs/src/test/resources/gtfs/rakvere-smoke/`
- Fixture is deterministic and intentionally small.
- It is not a production data snapshot.
- Synthetic fixture stop names are not authoritative city metadata for real Rakvere mapping.
- PASS 20 integration tests prove parser/mapper output can seed feature-search pipeline:
  - `MappedGtfsFeed.stopPoints` seeding `InMemoryStopPointIndex`
  - `MappedGtfsFeed.routePatterns` supplied to `DirectRouteQueryPreparationUseCase`
- This integration proof does not implement production feed ingestion, provider abstractions, or cache wiring.
- PASS 20 tests do not use real `rakvere.zip`; they use `rakvere-smoke` fixture only.

## PASS 21 Feed Boundary

- PASS 21 introduces parser-agnostic `DomainFeedSnapshot` and `DomainFeedSnapshotProvider` in `feature-search`.
- Callers may convert parser output into snapshot boundary objects:
  - `DomainFeedSnapshot(cityId, mappedFeed.stopPoints, mappedFeed.routePatterns)`
- PASS 21 does not implement production ingestion, downloader, or Room-backed provider wiring.

## PASS 22A Identity Strategy

- GTFS `stop_id` and trip-derived route-pattern IDs are treated as feed/city-local storage identifiers.
- Future Room schema keys must be city/feed-scoped:
  - stop point storage key: `cityId + feedId + stopId`
  - route pattern storage key: `cityId + feedId + patternId`
  - pattern stop storage key: `cityId + feedId + patternId + sequence`
- This strategy does not change parser behavior or routing identity semantics.

## PASS 17 Metadata Discovery Note

- Real `rakvere.zip` `stops.txt` was inspected in a temp folder for conservative Rakvere POI stop-name discovery.
- Discovery output is used only for metadata hints (`preferredStopGroupNames`), not parser/runtime behavior.
- Downloaded ZIP/data files remain outside the repository and must not be committed.

## Expected Full Pipeline (Future)

1. Discover/select feed set by city mapping metadata.
2. Download feed(s) and collect provenance metadata.
3. Validate integrity and required schema files.
4. Parse/map to canonical domain.
5. Persist to Room cache.
6. Expose offline-first query surfaces.
7. Optionally layer realtime by city adapter.

## Not Implemented Yet

- Network downloader/update checks.
- ZIP ingestion from live sources in runtime.
- Room/cache persistence.
- Room-backed feed snapshot/provider boundary implementation.
- Shapes/fares/transfers handling.
- Realtime ingestion.
- Production feed-to-city adapter orchestration.

## Pipeline Rules

- Static GTFS remains canonical base.
- Multi-feed ingestion is expected (not one guaranteed national ZIP).
- StopPoint precision must be preserved through mapping.
- `calendar_dates` override behavior remains mandatory.
