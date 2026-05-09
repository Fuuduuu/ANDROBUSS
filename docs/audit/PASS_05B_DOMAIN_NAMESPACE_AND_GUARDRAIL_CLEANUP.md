# PASS 05B DOMAIN_NAMESPACE_AND_GUARDRAIL_CLEANUP

## Objective

Resolve `core-domain` namespace inconsistency before PASS 06 and add small guardrails/docs cleanup without adding new product logic.

## Repo Guard Result

- `pwd`: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- `git rev-parse --show-toplevel`: `C:/Users/Kasutaja/Desktop/ANDROBUSS`
- `git status --short --untracked-files=all`: clean before changes
- `git branch --show-current`: `main`
- `git remote -v`: `origin https://github.com/Fuuduuu/ANDROBUSS.git` (fetch/push)
- `git rev-parse HEAD`: `d9213c8698d4787776db728ea8b94f76b3ccf07d`
- `git log --oneline -6`: inspected and consistent with PASS history

## Audit Finding Addressed

- MEDIUM finding: `core-domain` used `ee.fuuduu.androbuss.core.domain` while project namespaces elsewhere follow `ee.androbus.*`.

## Namespace Before/After

- Before:
  - path: `core-domain/src/main/kotlin/ee/fuuduu/androbuss/core/domain/`
  - package: `ee.fuuduu.androbuss.core.domain`
- After:
  - path: `core-domain/src/main/kotlin/ee/androbus/core/domain/`
  - package: `ee.androbus.core.domain`

## Guardrails Added

- `.gitignore`: added `*.zip` to reduce risk of committing downloaded GTFS/source archives.
- `docs/TRUTH_INDEX.md`: added StopGroup/StopPoint displayName clarification while preserving `StopPointId` as routing identity.
- `docs/TESTING_STRATEGY.md`: added PASS 05 no-tests note and PASS 06 test-infra requirement.
- `docs/MERMAID_DIAGRAMS.md`: pass workflow updated through PASS 05B and PASS 06.

## Files Changed

- `.gitignore`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/DataConfidence.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/GeoPoint.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/PatternStop.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/RouteLine.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/RoutePattern.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/ServiceModels.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/StopGroup.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/StopPoint.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/TransitIds.kt`
- `core-domain/src/main/kotlin/ee/androbus/core/domain/Trip.kt`
- `docs/CURRENT_STATE.md`
- `docs/TRUTH_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/MERMAID_DIAGRAMS.md`
- `docs/ROADMAP.md`
- `docs/audit/PASS_05B_DOMAIN_NAMESPACE_AND_GUARDRAIL_CLEANUP.md`

## Validation Result

- Initial attempt without shell-scoped Java setup:
  - `.\gradlew.bat :core-domain:build` -> failed (`JAVA_HOME is not set`).
  - `.\gradlew.bat build` -> failed (`JAVA_HOME is not set`).
- Final pass validation with shell-scoped Java:
  - `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot`
  - `java -version` -> `openjdk version "17.0.19" 2026-04-21` (Temurin 17.0.19+10)
  - `.\gradlew.bat :core-domain:build` -> **BUILD SUCCESSFUL**
  - `.\gradlew.bat build` -> **BUILD SUCCESSFUL**
- `git diff --check` -> no patch-format errors.
- `git status --short --untracked-files=all` -> expected PASS 05B file changes only.
- Note: Gradle emitted compileSdk/AGP compatibility warning (`compileSdk 35` with AGP `8.5.2`) but build passed.

## No Product Logic Confirmation

- No GTFS parser code added.
- No routing engine code added.
- No ServiceCalendarResolver implementation added.
- No Room schema/entities/DAOs added.
- No UI/ViewModel/feature logic added.
- No Android dependency introduced into `core-domain`.

## Risks / Unknowns

- If any external imports reference the old package path, follow-up rename in dependent modules may be needed when those modules begin using `core-domain`.
- PASS 06 still needs minimal test dependency setup before executable core-domain tests can be introduced.

## Recommended Next Pass

`PASS 06 — SERVICE_CALENDAR_RESOLVER_SPEC_AND_TESTS`
