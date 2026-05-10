# INVARIANTS

## Rule
An invariant is binding only when it is linked to an existing test or explicitly marked `TODO_TEST_REQUIRED`.

## Core routing / identity

INV-001 - StopPointId source
Rule:
StopPointId must come only from actual StopPoint.id or persisted stopId originally mapped from StopPoint.id.
Never:
- `StopPointId(displayName)`
- `StopPointId(stopGroupName)`
- `StopPointId(placeName)`
- `StopPointId(manualText)`
- `StopPointId(coordinate)`
Tests:
- `StopCandidateEnricherTest` - `anti fabrication guard keeps verified stop id only`
- `StopResolutionBridgeIntegrationTest` - `anti fabrication guard keeps GTFS stop ids only`

INV-002 - Android-free core
Rule:
`core-domain`, `core-gtfs`, `core-routing`, and `city-adapters` must not depend on `android.*` / AndroidX runtime APIs.
Tests:
- `GtfsDomainMapperTest` - `parser and mapper public APIs remain Android-free`
- `DirectRouteSearchTest` - `core routing remains Android free`
- `CityAdapterRegistryTest` - `city adapter metadata remains android free`
- `TODO_CORE_DOMAIN_ANDROID_FREE_TEST_REQUIRED`

INV-003 - Same-name stops remain separate
Rule:
Same public stop name may map to multiple StopPoints and must not be collapsed for routing.
Tests:
- `GtfsDomainMapperTest` - `same-name stops with different stop_id remain separate StopPoints`
- `GtfsFixtureSearchPipelineIntegrationTest` - `AnniVibe lesson - same-name Keskpeatus resolves to two distinct stop ids`

INV-004 - RoutePattern order
Rule:
RoutePattern / PatternStop sequence order must be preserved through parser, mapper, snapshot, Room, and routing layers.
Tests:
- `DomainInvariantsTest` - `RoutePattern preserves ordered stops`
- `GtfsDomainMapperTest` - `mapper creates RoutePattern ordered by stop sequence`

INV-005 - Loop duplicate StopPointIds
Rule:
A RoutePattern may contain the same StopPointId more than once.
Tests:
- `DomainInvariantsTest` - `RoutePattern allows duplicate StopPointIds for loop compatibility`
- `DirectRouteSearchTest` - `duplicate StopPointId loop pattern still finds valid later segment`

INV-006 - Feed-scoped storage identity
Rule:
Persistence storage identity must use cityId + feedId + localId.
Examples:
- StopPoint storage key: cityId + feedId + stopId
- RoutePattern storage key: cityId + feedId + patternId
- PatternStop storage key: cityId + feedId + patternId + sequence
Tests:
- `TODO_PASS_22B_TEST_REQUIRED`

INV-007 - Neutral feed snapshot contract
Rule:
DomainFeedSnapshot and DomainFeedSnapshotProvider must live in a neutral/core boundary before data-local implements Room provider behavior.
Tests:
- `TODO_PASS_22B_TEST_REQUIRED`

## Search / bridge

INV-008 - Bridge preconditions
Rule:
Direct route search must not run when origin/destination candidates do not have explicit StopPointIds or when RoutePatterns are missing.
Tests:
- `DirectRouteQueryBridgeTest` - `both unresolved returns BothUnresolved`
- `DirectRouteQueryBridgeTest` - `resolved candidates with empty patterns returns NoPatternsAvailable and does not call search`

## Data confidence

INV-009 - Realtime honesty
Rule:
Do not label schedule-only results as realtime.
Tests:
- `TODO_FUTURE_REALTIME_TEST_REQUIRED`