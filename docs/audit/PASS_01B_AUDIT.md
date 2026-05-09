# PASS_01B_AUDIT

Pass: PASS 01B — ANDROBUSS_ARCHITECTURE_REVIEW_FIXES  
Type: docs

## Scope Confirmation

This pass applied architecture review fixes in documentation only.

Touched files:

- `docs/CODEBASE_IMPACT_MAP.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/ANDROID_ARCHITECTURE.md`
- `docs/PROTECTED_SURFACES.md`
- `docs/TRUTH_INDEX.md`
- `docs/CURRENT_STATE.md`
- `docs/audit/PASS_01B_AUDIT.md`

## Fix Verification

1. CODEBASE_IMPACT_MAP dependency direction fixed: yes.
- `feature-stop-board` depends on `core-domain` (not `data-local`).
- `data-remote` and `city-adapters` are now explicitly orchestrated by app/sync composition.
- Direct `data-remote` <-> `city-adapters` dependency is prohibited in text and removed from diagram edges.

2. MERMAID diagrams corrected: yes.
- High-level diagram keeps domain on repository interfaces only.
- GTFS feed is represented in ingestion flow (`data-remote` / `core-gtfs` path), not as a domain dependency.
- Module dependency graph now matches corrected dependency rules.

3. Pure Kotlin Core Rule added: yes.
- Added named rule in `docs/ANDROID_ARCHITECTURE.md`.
- Includes no `android.*`, no `Context`, no lifecycle deps, violation is build-breaking architecture error.
- Includes Kotlin/JVM/KMP compatibility note.

4. Protected surface gate added: yes.
- Added formal rule requiring dedicated `PROTECTED_SURFACE_CHANGE` pass plus docs-only impact review.
- Explicit protected list includes ServiceCalendarResolver, Room schema, routing engine interface, canonical model, city adapter contract, GTFS parser, and location/privacy handling.

5. TRUTH_INDEX missing canonical truths added: yes.
- Added canonical Stop vs StopPoint mapping definition.
- Added service calendar exception precedence and exception type semantics.
- Added confidence tiers (`REALTIME`, `FORECAST`, `STATIC`) with TODO thresholds before realtime rollout.

## Non-Code Safety Check

No runtime code, Gradle files, module source files, or build system artifacts were created in this pass.
