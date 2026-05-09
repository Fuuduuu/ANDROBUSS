# MERMAID_DIAGRAMS

Mermaid diagrams in this file are the source of truth for architecture visuals in PASS 01 and PASS 01B.

## High-Level System Architecture

```mermaid
flowchart TD
    User[Passenger] --> UI[Compose Material 3 UI]
    UI --> VM[MVVM ViewModels]
    VM --> Domain[Domain Use Cases and Policies]
    Domain --> RepoContracts[Repository Interfaces]

    LocalImpl[data-local Repository Implementations] --> RepoContracts
    RemoteImpl[data-remote Repository Implementations] --> RepoContracts

    RemoteImpl --> GTFSIngest[GTFS Ingestion Pipeline]
    GTFSIngest --> CoreGTFS[core-gtfs Parser and Mapper]
    CoreGTFS --> NationalFeed[National Static GTFS]

    LocalImpl --> RoomStore[(Room Offline Cache)]
```

## Android Module Dependency Graph

```mermaid
graph TD
    app --> feature_map[feature-map]
    app --> feature_search[feature-search]
    app --> feature_stop_board[feature-stop-board]
    app --> feature_route_detail[feature-route-detail]
    app --> feature_favourites[feature-favourites]
    app --> feature_alerts[feature-alerts]
    app --> data_local[data-local]
    app --> data_remote[data-remote]
    app --> city_adapters[city-adapters]

    feature_map --> core_routing[core-routing]
    feature_search --> core_domain[core-domain]
    feature_stop_board --> core_domain
    feature_route_detail --> core_routing
    feature_favourites --> core_domain
    feature_alerts --> core_domain

    core_routing --> core_domain

    data_local --> core_gtfs[core-gtfs]
    data_local --> core_domain

    data_remote --> core_gtfs
    data_remote --> core_domain

    city_adapters --> core_gtfs
    city_adapters --> core_domain
```

## GTFS Ingestion Flow

```mermaid
flowchart TD
    Source[National Static GTFS] --> Download[WorkManager Sync Job]
    Download --> Validate[Schema and Integrity Validation]
    Validate --> Diff[Snapshot Diff Detection]
    Diff --> Parse[core-gtfs Parser]
    Parse --> Normalize[Canonical Model Mapping]
    Normalize --> Persist[(Room Cache Write)]
    Persist --> Query[Offline-first Query Surfaces]

    CityRealtime[Optional City Realtime Feed] --> AdapterMap[City Adapter Mapping]
    AdapterMap --> Query
```

## Direct Route Candidate Search Flow

```mermaid
flowchart TD
    Start[Destination Selected] --> Locate[Resolve nearby StopPoints]
    Locate --> CandidateStops[Find origin candidate stops]
    CandidateStops --> DirectSearch[Search direct trips only]
    DirectSearch --> ActiveFilter[ServiceCalendarResolver filters active service]
    ActiveFilter --> Rank[Rank by walk distance, departure, ride quality]
    Rank --> Found{Any direct routes?}
    Found -->|Yes| RouteCard[Return route cards]
    Found -->|No| NoDirect[Return no direct result; keep UI actionable]
    NoDirect --> TransferLater[One-transfer search is later pass]
```

## Pass Workflow

```mermaid
flowchart LR
    P00[PASS 00 Repo Bootstrap Docs done] --> P01[PASS 01 Android App Architecture Docs done]
    P01 --> P01B[PASS 01B Architecture Review Fixes current]
    P01B --> P02[PASS 02 Repo Skeleton and Build]
    P02 --> P03[PASS 03 GTFS Source Discovery]
    P03 --> P04[PASS 04 Core GTFS Parser]
    P04 --> P05[PASS 05 Core Domain Model]
    P05 --> P06[PASS 06 Calendar Resolver]
    P06 --> P07[PASS 07 Room Schema]
    P07 --> P08[PASS 08 Rakvere Adapter and GTFS Ingest]
    P08 --> P09[PASS 09 Direct Route Engine]
    P09 --> P10[PASS 10 Android Compose Skeleton with Stop Board]
```

## Protected Surfaces Diagram

```mermaid
flowchart TD
    Protected[Protected Surfaces] --> Model[Canonical Data Model]
    Protected --> Calendar[ServiceCalendarResolver]
    Protected --> RoutingAPI[Routing Engine Interface]
    Protected --> RoomSchema[Room Schema]
    Protected --> GTFSParser[GTFS Parser]
    Protected --> AdapterContract[City Adapter Contract]
    Protected --> Privacy[Location and Privacy Handling]

    GateRule[PROTECTED_SURFACE_CHANGE pass plus docs-only impact review required] --> Calendar
    GateRule --> RoutingAPI
    GateRule --> RoomSchema
    GateRule --> Model
    GateRule --> AdapterContract
    GateRule --> GTFSParser
    GateRule --> Privacy
```
