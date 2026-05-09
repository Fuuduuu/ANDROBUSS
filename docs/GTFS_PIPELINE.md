# GTFS_PIPELINE

Planned ingest/cache/diff flow only. No parser implementation exists yet.

## Planned Stages

1. Source registry resolution (city -> primary feed + optional secondary/context feed).
2. Source fetch from selected static GTFS feed set (unified and/or split feeds).
3. Integrity checks (HTTP status, content-type, checksum/hash, baseline schema sanity).
4. Dataset diff detection versus cached snapshot per feed.
5. Parse and normalize into canonical domain entities.
6. Persist to Room with migration-safe schema versioning.
7. Expose offline-first query surfaces to app features.
8. Optionally layer city realtime where adapter supports it.

## Parser Preconditions (PASS 04 Planning)

Minimum first-support file set for parser work:

- Mandatory:
  - `agency.txt`
  - `stops.txt`
  - `routes.txt`
  - `trips.txt`
  - `stop_times.txt`
- Service calendar requirement:
  - at least one of `calendar.txt` or `calendar_dates.txt` must exist
  - ANDROBUSS test fixtures should include both where possible
- Optional (first parser increment):
  - `shapes.txt`
  - `feed_info.txt`
  - `fare_attributes.txt`

Missing-file behavior requirements (planned):

- Missing mandatory file -> fail ingest with explicit structured error.
- Missing optional file -> continue with warning metadata.
- Missing both `calendar.txt` and `calendar_dates.txt` -> fail ingest.

Tracking and provenance requirements (planned):

- Persist feed provenance metadata alongside ingest result:
  - source URL
  - source authority
  - retrieval timestamp
  - file size
  - SHA256
  - row-count summary
- Keep feed-scoped metadata, not only global metadata.

Calendar rule requirement:

- `calendar_dates` exception override must be tested as priority over base `calendar`.

Stop precision requirement:

- Preserve StopGroup vs StopPoint separation through parse/normalize stages.
- Do not collapse same-name opposite-direction stop points into one record.

## Notes

- Static GTFS is canonical base.
- Realtime is optional per city.
- PASS 03 discovery indicates multi-feed reality; pipeline must not assume one immutable national ZIP.
- Route/authority-level filtering is required for city mapping because split feeds may still include a broad stop table.
