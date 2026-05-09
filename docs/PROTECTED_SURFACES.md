# PROTECTED_SURFACES

Future protected surfaces (high change-control sensitivity):

- Canonical transit domain data model.
- `ServiceCalendarResolver` behavior and edge-case handling.
- Routing engine interface.
- Room schema and migration strategy.
- GTFS parser correctness boundaries.
- City adapter contract and compatibility guarantees.
- Location and privacy handling policy.

## Formal Protection Rule

A protected surface may not change without a dedicated `PROTECTED_SURFACE_CHANGE` pass preceded by a docs-only impact review.

## Explicitly Protected Items

- `ServiceCalendarResolver`
- Room schema
- Routing engine interface
- Canonical data model
- City adapter contract
- GTFS parser
- Location/privacy handling
