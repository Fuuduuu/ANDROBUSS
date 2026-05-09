# GTFS_PIPELINE

Planned ingest/cache/diff flow only. No parser implementation exists yet.

## Planned Stages

1. Source fetch from canonical national static GTFS.
2. Integrity checks (availability, schema sanity, checksum/hash).
3. Dataset diff detection versus cached snapshot.
4. Parse and normalize into canonical domain entities.
5. Persist to Room with migration-safe schema versioning.
6. Expose offline-first query surfaces to app features.
7. Optionally layer city realtime where adapter supports it.

## Notes

- Static GTFS is canonical.
- Realtime is optional per city.
