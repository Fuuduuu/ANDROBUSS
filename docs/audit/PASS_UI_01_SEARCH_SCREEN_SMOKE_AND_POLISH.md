# PASS_UI_01_SEARCH_SCREEN_SMOKE_AND_POLISH

## Objective

Polish the first Compose search screen without expanding runtime scope or changing core/search/data behavior.

## Repo Guard Result

- Repo root: `C:\Users\Kasutaja\Desktop\ANDROBUSS`
- Branch: `main`
- Remote: `https://github.com/Fuuduuu/ANDROBUSS.git`
- HEAD at start: `e67e750d7cb2d150239df9116beed4bf88c71044`
- Working tree at start: clean
- `py -3 tools/validate_project_state.py` gate: passed

## UI Changes

- Search button (`Otsi`) now stays disabled until:
  - destination state is `DestinationInputState.Resolved`
  - origin is selected (`originStopPointId != null`)
- `FeedState.Ready` banner is hidden (no card/banner rendered in ready state).
- Technical wording removed for destination-not-ready state:
  - from `Sihtkoht pole route-otsinguks valmis.`
  - to `Vali esmalt sihtkoht.`
- Route-found section no longer exposes raw machine IDs:
  - removed direct rendering of `routePatternId.value`, `originStopPointId.value`, `destinationStopPointId.value`
  - now renders user-facing summary:
    - `✓ Marsruut leitud`
    - `Vahepeatusi: X`
    - `(Täielikud peatusenimed ja sõiduajad on tulemas)`

## Tests Added/Updated

Updated `SearchScreenStateTextTest` with smoke/polish guards:

- search button disabled when destination is empty
- search button disabled when origin is missing
- search button enabled when destination is resolved and origin is selected
- destination-not-ready message does not contain `route`
- route-found summary does not expose raw stop IDs
- `FeedState.Ready` banner hidden behavior
- existing static-schedule wording and explicit destination action checks remain

## Validation Result

Commands run:

- `py -3 tools/validate_project_state.py`
- `.\gradlew.bat :app:test`
- `.\gradlew.bat detekt`
- `.\gradlew.bat build`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`
- `rg -n "route-otsinguks" app/src/main`
- `rg -n "routePatternId\.value|originStopPointId\.value|destinationStopPointId\.value" app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt`
- `rg -n "LocationManager|FusedLocation|ACCESS_FINE_LOCATION|ACCESS_COARSE_LOCATION|WorkManager|Retrofit|OkHttp|GTFSRealtime|TripUpdate|VehiclePosition" app/src/main`

Result:

- Build/test/detekt passed.
- Boundary greps for forbidden terms in this pass scope are clean.
- No source changes outside allowed files.

## Forbidden Scope Confirmation

- No `SearchViewModel` or `SearchUiState` changes.
- No feature/core/data-local/data-remote/city-adapters source changes.
- No navigation/GPS/map/network/realtime/WorkManager changes.
- No Room/schema/routing logic changes.
- No build/lockfile/asset/Hilt module changes.

## Files Changed

- `app/src/main/kotlin/ee/androbus/app/presentation/search/SearchScreen.kt`
- `app/src/test/kotlin/ee/androbus/app/presentation/search/SearchScreenStateTextTest.kt`
- `docs/PROJECT_STATE.yml`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/AUDIT_INDEX.md`
- `docs/TESTING_STRATEGY.md`
- `docs/audit/PASS_UI_01_SEARCH_SCREEN_SMOKE_AND_POLISH.md`

## Next Recommended Pass

- `PASS_29_RAKVERE_QUICK_DESTINATIONS_SCOPE_AUDIT`
