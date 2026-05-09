# TESTING_STRATEGY

## Planned Test Layers

- Unit tests for core-domain invariants.
- Unit tests for GTFS parsing and mapping.
- Unit/property tests for calendar and service-date resolution.
- Unit tests for routing candidate generation and ranking.
- Compose UI tests in later passes.
- Field tests per city adapter.

## PASS 04 GTFS Fixture Strategy

### Fixture Types

A. Smoke fixture:

- Tiny fixture for one city.
- One or two lines only.
- Enough records to validate parser reads core mandatory files.
- No routing complexity requirement.

B. StopPoint precision fixture:

- Must include same-name and opposite-direction stop cases (or synthetic equivalent later if source feed does not contain minimal case cleanly).
- Primary purpose: protect StopGroup vs StopPoint separation.

C. Direct-route fixture:

- Must include valid ordered pattern where `destinationIndex > originIndex`.
- Must include invalid reverse-direction case.

D. Calendar fixture:

- Must include `calendar.txt` and `calendar_dates.txt`.
- Must cover add/remove exception behavior (`exception_type=1` add, `exception_type=2` remove).

E. City-mapping fixture:

- Must prove city-to-feed mapping decisions.
- Must avoid parsing full Estonia bundle for normal unit tests.

## Fixture Anti-Goals (Do Not Add Yet)

- Full Estonia unified feed as normal unit-test fixture.
- Full multi-city GTFS dumps committed in repo.
- Realtime payload fixtures.
- Transfer/ticketing/map-tile data fixtures.
- Huge `stop_times.txt` fixtures where minimal deterministic subset is enough.
- Generated Room database snapshots.

## Fixture Storage Policy (Planned)

- No large raw ZIP files committed.
- Tiny deterministic fixtures only (hand-curated or reproducibly generated in a dedicated future pass).
- Real downloaded ZIPs stay temp/local/cache only.
- Source URL + hash + discovery timestamp must be recorded in docs/audit.

Potential future fixture paths (proposal only, not created in PASS 04):

- `test-fixtures/gtfs/rakvere-smoke/`
- `test-fixtures/gtfs/rakvere-stop-point-precision/`
- `test-fixtures/gtfs/rakvere-calendar/`
- `test-fixtures/gtfs/parnu-city-county-mapping/`

## Protected Coverage Target

- `ServiceCalendarResolver` target: 100% branch coverage.

## PASS 05 Test Infra Note

- PASS 05 intentionally shipped without executable `core-domain` tests because minimal test dependency wiring was missing and build-file changes were out of scope for that pass.
- PASS 06 must begin by adding minimal pure Kotlin test dependency support and executable tests for calendar semantics.

## PASS 06 Test Baseline

- `core-domain` now uses minimal pure Kotlin test infrastructure via `testImplementation(kotlin("test"))`.
- PASS 05 invariant tests are now executable in `core-domain`.
- `ServiceCalendarResolver` semantics are covered with explicit `LocalDate` tests:
  - base weekday/date-range activation
  - `ADD_SERVICE` and `REMOVE_SERVICE` override precedence
  - no-base-calendar behavior
  - exact service/date exception scoping
  - duplicate exception rejection policy

## Risk Focus

- Calendar edge cases (holidays, exceptions, overnight service).
- StopGroup versus StopPoint correctness.
- Offline cache consistency after dataset updates.
- City/feed mapping drift over time.
