# PASS_TEMPLATE

## 1. One-sentence goal
Exactly one sentence.

## 2. Allowed surfaces
List exact modules/files.

## 3. Forbidden surfaces
List exact protected surfaces.

## 4. Invariants that must survive unchanged
Reference INV IDs from `docs/INVARIANTS.md`.

## 5. Explicit non-goals
State what this pass does not do.

## 6. Required validation
List exact commands.

## 7. Rollback plan
Use `last_known_good_commit` from `docs/PROJECT_STATE.yml`.

## Codex prompt skeleton

PASS
TYPE
GATE
READ
TOUCH
NEVER
DO
VALIDATE
OUTPUT