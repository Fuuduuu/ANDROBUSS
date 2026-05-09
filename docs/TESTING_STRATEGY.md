# TESTING_STRATEGY

## Planned Test Layers

- Unit tests for GTFS parsing and mapping.
- Unit/property tests for calendar and service-date resolution.
- Unit tests for routing candidate generation and ranking.
- Compose UI tests in later passes.
- Field tests per city adapter.

## Protected Coverage Target

- `ServiceCalendarResolver` target: 100% branch coverage.

## Risk Focus

- Calendar edge cases (holidays, exceptions, overnight service).
- Stop versus stopPoint mapping correctness.
- Offline cache consistency after dataset updates.
