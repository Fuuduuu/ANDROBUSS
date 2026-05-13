# GTFS_PIPELINE

GTFS pipeline status after PASS 24 docs decision.

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

- PASS 21 introduced parser-agnostic `DomainFeedSnapshot` and `DomainFeedSnapshotProvider` boundary contracts.
- PASS 22B moved those contracts to `core-domain`.
- Callers may convert parser output into snapshot boundary objects:
  - `DomainFeedSnapshot(cityId, mappedFeed.stopPoints, mappedFeed.routePatterns)`
- PASS 21/22B do not implement downloader or production ingestion orchestration.

## PASS 22A/22B Identity + Storage Baseline

- GTFS `stop_id` and trip-derived route-pattern IDs are treated as feed/city-local storage identifiers.
- Room baseline schema keys are city/feed-scoped:
  - stop point storage key: `cityId + feedId + stopId`
  - route pattern storage key: `cityId + feedId + patternId`
  - pattern stop storage key: `cityId + feedId + patternId + sequence`
- PASS 22B adds `data-local` baseline Room entities/DAO/mapper/loader/provider using these scoped keys.
- Routing identity semantics remain unchanged (`StopPointId` still comes from persisted/verified stop IDs only).

## PASS 23 Import Writer Baseline

- PASS 23 adds `data-local` production `FeedSnapshotImporter`:
  - input: `DomainFeedSnapshot` + explicit `CityId` + explicit `FeedId`
  - write: scoped Room replace transaction in `FeedSnapshotDao`
- Importer remains parser-agnostic in production code:
  - no `MappedGtfsFeed`
  - no `GtfsFeedParser`
  - no `GtfsDomainMapper`
- PASS 23 integration coverage proves fixture-based roundtrip:
  - `GtfsFeedParser` + `GtfsDomainMapper` (test scope only)
  - `DomainFeedSnapshot`
  - Room importer write
  - Room provider prepare/load
  - feature-search route query preparation from Room-loaded patterns

## PASS 24 Bundled Asset Bootstrap Strategy

- MVP feed bootstrap source is a bundled APK asset, not a live network download.
- Bundled asset is produced offline from real GTFS and represented as serialized `DomainFeedSnapshot` (or equivalent app-readable format).
- PASS 24 does not choose JSON vs protobuf vs other serialization; that is PASS 25 scope.
- First launch / Room empty flow:
  1. app layer reads bundled feed asset
  2. app layer converts it to `DomainFeedSnapshot`
  3. app layer calls `FeedSnapshotImporter.import(cityId, feedId, snapshot)`
  4. app layer or search bootstrap owner calls `RoomDomainFeedSnapshotProvider.prepare(cityId, feedId)`
  5. after prepare, `getSnapshot(cityId)` may return non-null synchronously
- Production download/refresh remains future work via `data-remote` + WorkManager.
- This strategy satisfies offline-first MVP without requiring network bootstrap infrastructure.
- Real `rakvere.zip` itself must not be committed; bundled asset generation details are future pass work.
- Feed freshness/hash/version metadata is future and not part of PASS 24.

## PASS 25 Bundled Bootstrap Implementation Baseline

- PASS 25 implements a synthetic bundled JSON bootstrap in the app layer:
  - `app/src/main/assets/bootstrap/rakvere_bootstrap.json`
  - app DTOs (`BootstrapFeedDto`, `StopPointDto`, `RoutePatternDto`)
  - DTO -> `DomainFeedSnapshot` mapping without `core-domain` serialization annotations
  - `FeedBootstrapLoader` startup flow:
    1. read bundled asset
    2. decode DTO
    3. convert to `DomainFeedSnapshot`
    4. call `FeedSnapshotImporter.import(cityId, feedId, snapshot)`
    5. call `RoomDomainFeedSnapshotProvider.prepare(cityId, feedId)`
- `FeedBootstrapLoader.bootstrapIfNeeded()` is safe on repeated calls and handles missing asset as FeedNotReady-style no-crash return.
- `AndrobussApplication` performs pre-Hilt bootstrap on background dispatcher.
- `AppDatabase.create(context)` exists as temporary pre-Hilt factory.
- PASS 25 does not use real `rakvere.zip`, does not invoke parser in app production code, and does not add freshness/hash/version metadata.

## GTFS Data Legal Status for Production Bundled Assets

- Current app bundled asset is synthetic and is not real Rakvere production data.
- Real Rakvere bundled asset remains future work.
- Before committing a real serialized Rakvere `DomainFeedSnapshot` asset, the project must document:
  - source of data,
  - license terms,
  - attribution requirements,
  - whether bundling inside an APK is permitted.
- Raw `rakvere.zip` must not be committed.
- PASS 26 must resolve/document this legal/data-source status before PASS 26A real-asset generation.

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
- feed freshness/version metadata.
- app runtime wiring that prepares and consumes Room snapshot provider.
- Shapes/fares/transfers handling.
- Realtime ingestion.
- Production feed-to-city adapter orchestration.

## Pipeline Rules

- Static GTFS remains canonical base.
- Multi-feed ingestion is expected (not one guaranteed national ZIP).
- StopPoint precision must be preserved through mapping.
- `calendar_dates` override behavior remains mandatory.
