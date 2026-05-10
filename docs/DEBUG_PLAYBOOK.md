# DEBUG_PLAYBOOK

## Principle
Do not give AI the whole project history for debugging.
Give a compact DEBUG_PACKET.

## DEBUG_PACKET template

- Project / module:
- Last known good commit:
- Current failing commit:
- Exact failing command:
- Expected behavior:
- Actual behavior:
- Suspected layer:
- Allowed debug surfaces:
- Forbidden surfaces:
- Task:
- Minimal patch rule:

ANDROBUSS examples:
- feature-search bridge failure: suspected layer `feature-search/src/main/kotlin/ee/androbus/feature/search/bridge/`.
- core-routing protected surface: suspected layer `core-routing/src/main/kotlin/ee/androbus/core/routing/`.
- future Room/data-local failure: suspected layer `data-local/src/main/kotlin/ee/androbus/data/local/`.

## Minimal patch rule
If the fix needs more than roughly 20 lines outside the suspected layer, STOP and report architecture risk.

## Git bisect protocol

```powershell
git bisect start
git bisect bad HEAD
git bisect good <last_known_good_commit>
# run failing test command
git bisect good
# or
git bisect bad
git bisect reset
```

## Output format for debug fixes

- Root cause
- Minimal patch
- Regression test
- Checks result
- Risk remaining
- Commit recommendation