# GTFS_PIPELINE

GTFS pipeline status after PASS 07 and PASS 08.

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
- Shapes/fares/transfers handling.
- Realtime ingestion.
- Production feed-to-city adapter orchestration.

## Pipeline Rules

- Static GTFS remains canonical base.
- Multi-feed ingestion is expected (not one guaranteed national ZIP).
- StopPoint precision must be preserved through mapping.
- `calendar_dates` override behavior remains mandatory.
