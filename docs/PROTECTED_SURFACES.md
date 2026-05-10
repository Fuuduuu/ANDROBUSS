# PROTECTED_SURFACES

Protected surfaces (high change-control sensitivity):

- Canonical core domain model (`StopGroup` / `StopPoint` / `RoutePattern` semantics).
- `ServiceCalendarResolver` semantics and duplicate-exception policy.
- GTFS ingestion boundary in `core-gtfs`:
  - `CsvTableReader`
  - `GtfsFeedParser`
  - `GtfsDomainMapper`
- Direct routing boundary in `core-routing`:
  - `DirectRouteSearch`
  - direct-route result/reason contract
- Stop-point resolution boundary in `feature-search`:
  - `StopPointResolver` contract
  - `VerifiedStopPointCandidate` identity/source guarantees
  - `InMemoryStopPointIndex` anti-fabrication and multi-candidate behavior
- Room schema and migration strategy (future).
- City adapter contract and city/feed mapping model.
- Location/privacy handling policy.
- Synthetic fixture boundary:
  - `core-gtfs/src/test/resources/gtfs/rakvere-smoke/`

## Formal Protection Rule

A protected surface may not change without a dedicated `PROTECTED_SURFACE_CHANGE` pass preceded by a docs-only impact review.

## Scope Guardrails

- Docs-only passes must not alter runtime source/build logic.
- Runtime passes must not rewrite unrelated docs; only state/audit/roadmap updates are allowed unless the pass explicitly includes docs synchronization.

## UX Surface Guardrails

- Future UI surfaces must preserve destination-first behavior as default.
- Map-first UI requires explicit scope approval in a dedicated pass.
- Full timetable-first primary UI is not MVP-approved and must not be introduced implicitly.
