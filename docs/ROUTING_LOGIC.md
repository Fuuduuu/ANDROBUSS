# ROUTING_LOGIC

## Core Principles

- Destination-first interaction.
- Never return early with "busse pole" before meaningful candidate search.
- Direct route search is first priority.
- One-transfer search is a later wave extension.

## Output Contract (Route Card)

Each result should answer:

- Where to walk.
- Which line to board.
- When to board.
- Where to exit.

Avoid exposing raw `stopId` values or coordinate dumps to users.
