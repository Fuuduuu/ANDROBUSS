# MERMAID_DIAGRAMS

Mermaid diagrams in this file are the source of truth for architecture visuals after PASS UX-01.

## Pass Timeline

```mermaid
flowchart LR
    P00["PASS 00\ndocs bootstrap"] --> P01B["PASS 01B\narchitecture review fixes"]
    P01B --> P02["PASS 02\nrepo skeleton + build"]
    P02 --> P03["PASS 03\nGTFS source discovery"]
    P03 --> P04["PASS 04\nfixture strategy + city mapping"]
    P04 --> P05["PASS 05\ncore-domain stop/pattern models"]
    P05 --> P05B["PASS 05B\nnamespace + guardrails"]
    P05B --> P06["PASS 06\ncalendar resolver + tests"]
    P06 --> P07["PASS 07\nminimal GTFS fixture parser"]
    P07 --> P08["PASS 08\ndirect route search core"]
    P08 --> P09["PASS 09\nRakvere city adapter metadata"]
    P09 --> P10["PASS 10\ndestination target resolver"]
    P10 --> P11["PASS 11\nplace-to-stop candidate mapping"]
    P11 --> P12["PASS 12\norigin candidate resolver"]
    P12 --> P13["PASS 13\ndirect-route bridge + preconditions"]
    P13 --> P14["PASS 14\nstop-point resolution contract + name index"]
    P14 --> P15["PASS 15\nresolution-bridge integration tests"]
    P15 --> PUX01["PASS UX-01\ndocs-only UX blueprint sync"]
    PUX01 --> P16["PASS 16 planned\nproduction enrichment + bridge wiring"]
```

## Android Module Dependency Graph

```mermaid
graph TD
    app --> feature_map["feature-map (planned)"]
    app --> feature_search["feature-search (planned)"]
    app --> feature_stop_board["feature-stop-board (planned)"]
    app --> feature_route_detail["feature-route-detail (planned)"]
    app --> feature_favourites["feature-favourites (planned)"]
    app --> feature_alerts["feature-alerts (planned)"]
    app --> data_local["data-local (planned)"]
    app --> data_remote["data-remote (planned)"]
    app --> city_adapters["city-adapters (metadata next)"]

    feature_map --> core_routing["core-routing (implemented)"]
    feature_route_detail --> core_routing

    feature_search --> core_domain["core-domain (implemented)"]
    feature_stop_board --> core_domain
    feature_favourites --> core_domain
    feature_alerts --> core_domain

    core_routing --> core_domain
    core_gtfs["core-gtfs (implemented)"] --> core_domain

    data_local --> core_domain
    data_local --> core_gtfs
    data_remote --> core_domain
    data_remote --> core_gtfs

    city_adapters --> core_domain
    city_adapters --> core_gtfs
```

## GTFS Data Pipeline Baseline

```mermaid
flowchart LR
    Source["GTFS source discovery\n(ministry authority + verified hosting)"] --> Fixture["Synthetic tiny fixture\n(rakvere-smoke)"]
    Fixture --> Parser["GtfsFeedParser"]
    Parser --> Raw["GtfsModels"]
    Raw --> Mapper["GtfsDomainMapper"]
    Mapper --> Domain["StopPoint / RoutePattern / Trip / ServiceCalendar"]
    Domain --> Direct["DirectRouteSearch"]
    Direct --> Future["Later: city adapters + cache + UI"]
```

## Direct Route Algorithm

```mermaid
flowchart TD
    Input["origin StopPointId + destination StopPointId"] --> Scan["scan RoutePatterns"]
    Scan --> OriginIdx["collect all origin indices"]
    Scan --> DestIdx["collect all destination indices"]
    OriginIdx --> Pair["find pair where destinationIndex > originIndex"]
    DestIdx --> Pair
    Pair --> Valid{"valid pair found?"}
    Valid -->|Yes| Candidate["DirectRouteCandidate"]
    Valid -->|No| Reason["NotFound reason\n(SAME_STOP / ORIGIN_NOT_FOUND / DESTINATION_NOT_FOUND / DESTINATION_NOT_AFTER_ORIGIN / NO_DIRECT_PATTERN)"]
```

## Calendar Resolver Semantics

```mermaid
flowchart TD
    Base["ServiceCalendar base rule\n(weekday + start/end)"] --> Merge["ServiceCalendarResolver"]
    Exception["ServiceCalendarException\n(calendar_dates)"] --> Merge
    Merge --> Override{"exception exists for service/date?"}
    Override -->|ADD_SERVICE| Active["result = true"]
    Override -->|REMOVE_SERVICE| Inactive["result = false"]
    Override -->|No exception| BaseResult["use base calendar result"]
    BaseResult --> Output["LocalDate-based active/inactive"]
```

## Future City Adapter Path (Metadata First)

```mermaid
flowchart LR
    Rakvere["PASS 09 target\nRakvere metadata"] --> Mapping["city aliases + POI/source mapping + confidence"]
    Mapping --> FeedSelect["feed selection metadata"]
    FeedSelect --> LaterSearch["later destination-first search integration"]
    LaterSearch --> LaterUI["later UI integration"]
```

## UX Flow (Destination-First MVP)

```mermaid
flowchart LR
    Open["First open"] --> Ask["Kuhu soovid minna?"]
    Ask --> Input["Quick destination buttons or search"]
    Input --> Origin["Origin seed (manual text or location seed)"]
    Origin --> Candidates["Route candidate query path"]
    Candidates --> Cards["1-3 result cards with large ETA"]
    Cards --> Detail["Route detail (progressive disclosure)"]
    Detail --> Map["Optional map helper view"]
```
