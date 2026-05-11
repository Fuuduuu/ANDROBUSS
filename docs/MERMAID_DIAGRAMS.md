# MERMAID_DIAGRAMS

Synchronized after `PASS 24` candidate.

## Pass Timeline (Latest)

```mermaid
flowchart LR
    P21["PASS 21"] --> P22A["PASS 22A"]
    P22A --> P22B["PASS 22B"]
    P22B --> P23["PASS 23"]
    P23 --> P24["PASS 24 (docs-only)"]
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

## Feed Bootstrap and Runtime Load Flow (PASS 24 Decision)

```mermaid
sequenceDiagram
    participant App as app layer
    participant Importer as FeedSnapshotImporter
    participant Dao as FeedSnapshotDao
    participant Provider as RoomDomainFeedSnapshotProvider
    participant Search as feature-search caller

    Note over App: First launch / Room empty
    App->>App: Read bundled DomainFeedSnapshot asset
    App->>Importer: import(cityId, feedId, snapshot)
    Importer->>Dao: replaceSnapshot(cityId, feedId, ...)
    Note over Search: Before first search
    Search->>Provider: prepare(cityId, feedId)
    Provider->>Dao: load scoped snapshot
    Provider-->>Search: cached
    Search->>Provider: getSnapshot(cityId)
    Provider-->>Search: DomainFeedSnapshot? (cache-only)
```

`getSnapshot(cityId)` is cache-only; Room IO belongs to `prepare(...)`.
