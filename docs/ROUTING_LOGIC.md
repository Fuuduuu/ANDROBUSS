# ROUTING_LOGIC

## Product Direction

- Destination-first interaction remains the target UX.
- Avoid premature "busse pole" outcomes.
- Direct route search is first routing capability.
- Transfer routing is a later pass.

## Implemented Core (PASS 08)

Direct route is valid only when:
- origin `StopPointId` and destination `StopPointId` are in the same `RoutePattern`,
- and destination occurs after origin in that pattern.

Identity rules:
- routing identity is `StopPointId`,
- stop names/display labels are never routing identity.

## Deterministic Not-Found Order

- `SAME_STOP`
- `ORIGIN_NOT_FOUND`
- `DESTINATION_NOT_FOUND`
- `DESTINATION_NOT_AFTER_ORIGIN`
- `NO_DIRECT_PATTERN`

## Duplicate StopPoint Loop Policy

- Duplicate `StopPointId` values in one `RoutePattern` are allowed.
- Search evaluates all origin indices and all destination indices.
- A route is valid if any destination index is greater than any origin index.
- Earliest valid pair per pattern is used in current implementation.

## Not Implemented Yet

- Time-aware/schedule-aware route filtering.
- Service-day filtering inside route search.
- Transfer routing.
- Nearest-stop and walking-distance ranking.
- UI card composition and rider copy generation.
