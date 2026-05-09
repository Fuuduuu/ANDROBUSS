# GTFS_PIPELINE

Planned ingest/cache/diff flow only. No parser implementation exists yet.

## Planned Stages

1. Source registry resolution (city -> primary feed + optional supplemental feed).
2. Source fetch from selected static GTFS feed set (unified and/or split feeds).
3. Integrity checks (HTTP status, content-type, schema sanity, checksum/hash).
4. Dataset diff detection versus cached snapshot per feed.
5. Parse and normalize into canonical domain entities.
6. Persist to Room with migration-safe schema versioning.
7. Expose offline-first query surfaces to app features.
8. Optionally layer city realtime where adapter supports it.

## Notes

- Static GTFS is canonical.
- Realtime is optional per city.
- PASS 03 discovery indicates multi-feed reality; pipeline must not assume one immutable national ZIP.
- Route/authority-level filtering is required for city mapping because split feeds may still include a broad stop table.
