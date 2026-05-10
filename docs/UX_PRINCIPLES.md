# UX_PRINCIPLES

## Product Principles

- Destination-first flow.
- Simple Estonian copy.
- Map is an input aid, not the primary cognitive burden.
- Route card must answer: where to walk, which line, when, where to exit.

## State Alignment (After PASS 11)

- Core data/routing foundations exist in pure Kotlin modules.
- Destination-target resolver logic exists in `feature-search`.
- Place-to-stop candidate mapping now exists as a name-only metadata bridge.
- Candidate mapping is not nearest-stop selection and not route search.
- No production UI flow is implemented yet.

## Information Discipline

- Do not expose raw stop IDs to riders.
- Do not expose raw coordinate tuples in primary UI.
- Prefer human-readable stop and route descriptions.
