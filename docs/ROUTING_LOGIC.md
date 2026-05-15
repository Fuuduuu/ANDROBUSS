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

## Destination Enrichment Orchestration (PASS 18)

- `DestinationEnrichmentOrchestrator` coordinates destination-candidate enrichment only.
- It does not call `DirectRouteQueryBridge`.
- It does not select a single verified stop-point candidate.
- `isAmbiguous=true` when total verified stop-point candidate count is greater than one.
- Candidate IDs can be extracted from enriched results as:
  - `enrichedCandidates.flatMap { it.verifiedCandidates.map { v -> v.stopPointId } }`
- Do not forward IDs blindly when `isAmbiguous=true`; disambiguation remains required before route-query preparation.

## Route Query Preparation Use-Case (PASS 19)

- `DirectRouteQueryPreparationUseCase` coordinates:
  - already computed `DestinationEnrichmentResult`,
  - explicit origin `StopPointId`,
  - caller-supplied `RoutePattern` list.
- `RoutePattern` loading is caller responsibility.
- The use-case does not call `DestinationEnrichmentOrchestrator`.
- The use-case does not load GTFS/Room data.
- Bridge call is allowed only when:
  - destination is enriched and non-ambiguous,
  - origin `StopPointId` is provided,
  - patterns are provided.
- Destination selection uses exact-one policy (`single()`), not fallback `first()`.

## GTFS Fixture Pipeline Integration (PASS 20)

- `DirectRouteSearch` behavior is unchanged by PASS 20.
- `GtfsFeedParser` + `GtfsDomainMapper` output can be consumed directly by feature-search pipeline tests:
  - `MappedGtfsFeed.stopPoints` -> `InMemoryStopPointIndex`
  - `MappedGtfsFeed.routePatterns` -> `DirectRouteQueryPreparationUseCase`
- `DirectRouteQueryPreparationUseCase` can execute bridge flow with parser-derived patterns when caller supplies them.
- `RoutePattern` source remains caller responsibility until provider/Room passes.
- PASS 20 uses synthetic `rakvere-smoke` stop names for integration tests; this does not redefine Rakvere real-world POI metadata.

## Feed Snapshot Boundary (PASS 21)

- `DirectRouteQueryPreparationUseCase` remains unchanged and still accepts caller-supplied `RoutePattern` lists.
- `DomainFeedSnapshot` groups `stopPoints` and `routePatterns` together for one city snapshot.
- `DomainFeedSnapshotProvider` is parser-agnostic and synchronous.
- Future caller wiring may source route patterns from `DomainFeedSnapshotProvider`; this pass does not load data automatically.

## Feed Contract Move + Room Provider Baseline (PASS 22B)

- `DomainFeedSnapshot` and `DomainFeedSnapshotProvider` now live in `core-domain`.
- `RoomDomainFeedSnapshotProvider` in `data-local` uses load-then-serve policy:
  - `prepare(cityId, feedId)` performs suspend Room load
  - `getSnapshot(cityId)` returns cache only and never hits Room
- This pass does not change route search, bridge, or route-query preparation behavior.

## Storage Key Strategy (PASS 22A/22B)

- Room storage keys are city/feed-scoped composites.
- `StopPointId` remains routing identity and is not derived from names or coordinates.

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

## Future Realtime Matching Note

- Current direct routing uses static `RoutePattern` stop order only.
- `stop_sequence` / order must remain preserved, especially for loop patterns with repeated stop IDs.
- Realtime `TripUpdate` matching is future-only and will require explicit `TripId + stop_sequence` modelling.
- Current routing must not infer realtime status from static results.

## App Route Query State Baseline (PASS 28B Accepted)

- App `SearchViewModel` route query trigger is explicit: `searchRoute()`.
- Route query does not auto-run on origin or destination input changes.
- Explicit origin is required; missing origin maps to `OriginNotProvided`.
- Feed/provider unavailable maps to `FeedNotAvailable`.
- Unresolved or ambiguous destination maps to `DestinationNotReady`.
- Empty pattern set maps to `NoPatternsAvailable`.
- These precondition states are distinct from `RouteNotFound`.
- Route execution path uses `DirectRouteQueryPreparationUseCase`.
- Destination IDs used for query come from verified candidate IDs, not display text.

## Not Implemented Yet

- Time-aware/schedule-aware route filtering.
- Service-day filtering inside route search.
- Transfer routing.
- Nearest-stop and walking-distance ranking.
- Production wiring of destination enrichment orchestration and bridge flow in app/ViewModel runtime.
- Production route-pattern provider and feed-domain integration for use-case caller.
- Verified origin-side stop-point enrichment pipeline.
- UI card composition and rider copy generation.
