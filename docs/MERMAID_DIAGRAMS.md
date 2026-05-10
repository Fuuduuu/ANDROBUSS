# MERMAID_DIAGRAMS

Synchronized after `PASS 20B`.

## Pass Timeline (Latest)

```mermaid
flowchart LR
    P17["PASS 17"] --> P18["PASS 18"]
    P18 --> P19["PASS 19"]
    P19 --> P20["PASS 20"]
    P20 --> P20B["PASS 20B (docs-only)"]
    P20B --> P21["PASS 21 (planned)"]
```

## PASS 20 Fixture-to-Search Pipeline

```mermaid
flowchart LR
    Fixture["rakvere-smoke fixture"] --> Parser["GtfsFeedParser"]
    Parser --> Mapper["GtfsDomainMapper"]
    Mapper --> Feed["MappedGtfsFeed"]
    Feed --> StopPoints["stopPoints"]
    Feed --> Patterns["routePatterns"]
    StopPoints --> Index["InMemoryStopPointIndex"]
    Index --> Enricher["StopCandidateEnricher"]
    Enricher --> Orchestrator["DestinationEnrichmentOrchestrator"]
    Patterns --> UseCase["DirectRouteQueryPreparationUseCase"]
    Orchestrator --> UseCase
    UseCase --> Bridge["DirectRouteQueryBridge"]
    Bridge --> Search["DirectRouteSearch"]
```

## StopPointId Source Safety

```mermaid
flowchart LR
    StopId["GTFS stop_id"] --> DomainId["StopPoint.id"]
    DomainId --> VerifiedId["VerifiedStopPointCandidate.stopPointId"]
    VerifiedId --> CandidateIds["StopCandidate.stopPointIds"]
    CandidateIds --> BridgeUse["bridge/use-case routing input"]

    Bad1["stop_name"] -. forbidden .-> BridgeUse
    Bad2["displayName"] -. forbidden .-> BridgeUse
    Bad3["POI name"] -. forbidden .-> BridgeUse
```

## Provider Boundary (Future)

```mermaid
flowchart LR
    Current["MappedGtfsFeed in tests"] --> FutureBoundary["Future DomainFeedSnapshot/provider boundary"]
    FutureBoundary --> FutureRoom["Future Room/cache provider"]
    FutureRoom --> FutureRuntime["Future runtime query source"]
```

## Implemented Modules After PASS 20

```mermaid
flowchart LR
    core_domain["core-domain"]:::done
    core_gtfs["core-gtfs"]:::done
    core_routing["core-routing"]:::done
    city_adapters["city-adapters"]:::done
    feature_search["feature-search"]:::done
    data_local["data-local"]:::future
    data_remote["data-remote"]:::future
    app_ui["app/UI feature modules"]:::future

    core_gtfs --> core_domain
    core_routing --> core_domain
    city_adapters --> core_domain
    feature_search --> core_domain
    feature_search --> core_routing
    feature_search --> city_adapters
    app_ui --> feature_search
    app_ui --> data_local
    app_ui --> data_remote

    classDef done fill:#eaf7ea,stroke:#3a7a3a,stroke-width:1px;
    classDef future fill:#f6f6f6,stroke:#8a8a8a,stroke-width:1px;
```
