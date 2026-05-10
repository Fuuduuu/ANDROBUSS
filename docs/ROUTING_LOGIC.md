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

## Bridge Preconditions (PASS 13)

- Direct route search must not run until explicit origin and destination `StopPointId` candidates exist.
- Bridge precondition outcomes:
  - both unresolved -> `BothUnresolved`
  - only origin unresolved -> `OriginUnresolved`
  - only destination unresolved -> `DestinationUnresolved`
  - no patterns -> `NoPatternsAvailable`
- Current selection policy is deterministic and minimal:
  - first resolved origin `StopPointId`
  - first resolved destination `StopPointId`
- Bridge does not derive `StopPointId` from names, stop-group labels, manual text, or coordinates.

## Destination-Side Enrichment (PASS 16)

- `DirectRouteSearch` behavior/signature is unchanged.
- `StopCandidateEnricher` can populate destination-side `StopCandidate.stopPointIds` before bridge query.
- Enrichment copies IDs only from `VerifiedStopPointCandidate.stopPointId`.
- `NotEnriched` preserves original unresolved candidate.
- Origin-side enrichment is not implemented.

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
- Production wiring of destination enrichment and bridge flow in app/ViewModel runtime.
- Verified origin-side stop-point enrichment pipeline.
- UI card composition and rider copy generation.
