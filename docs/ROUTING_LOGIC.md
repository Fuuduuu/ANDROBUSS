# ROUTING_LOGIC

## Core Principles

- Destination-first interaction.
- Never return early with "busse pole" before meaningful candidate search.
- Direct route search is first priority.
- One-transfer search is a later wave extension.

## PASS 08 Direct-Route Core Rule

- A direct route candidate is valid only when:
  - origin `StopPointId` and destination `StopPointId` are present in the same `RoutePattern`
  - and destination position is after origin position in that pattern.
- Routing identity is `StopPointId` only.
- Stop names/display labels must not be used as routing identity.

## PASS 08 Deterministic Not-Found Precedence

- `SAME_STOP`
- `ORIGIN_NOT_FOUND`
- `DESTINATION_NOT_FOUND`
- `DESTINATION_NOT_AFTER_ORIGIN` (shared pattern exists but only reverse order)
- `NO_DIRECT_PATTERN` (no pattern contains both stop points)

## PASS 08 Loop Policy

- Duplicate `StopPointId` values inside one `RoutePattern` are allowed.
- Direct search evaluates all origin and destination indices and accepts any pair where destination index is greater than origin index.

## Output Contract (Route Card)

Each result should answer:

- Where to walk.
- Which line to board.
- When to board.
- Where to exit.

Avoid exposing raw `stopId` values or coordinate dumps to users.
