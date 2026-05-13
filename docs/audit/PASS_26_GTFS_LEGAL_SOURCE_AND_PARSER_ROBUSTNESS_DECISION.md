# PASS_26_GTFS_LEGAL_SOURCE_AND_PARSER_ROBUSTNESS_DECISION

## Objective

Document legal/source/freshness constraints and parser-robustness requirements before any real Rakvere bundled asset generation.

## Sources/Findings Summarized

- Current app bootstrap asset is synthetic JSON, not real Rakvere production data.
- PASS 26 input findings indicate:
  - official Peatus/UTR GTFS endpoint exists,
  - public transport registry data is publicly available,
  - public-channel/mobile-app reuse requires source attribution,
  - public app data should not be older than 7 days from download.
- PASS 26 input findings also provide a real feed technical profile snapshot:
  - `feed_info.txt` present; publisher Regionaal- ja Pollumajandusministeerium,
  - `attributions.txt` missing,
  - `calendar_dates.txt` present, `frequencies.txt`/`transfers.txt` missing,
  - `shapes.txt` present,
  - `block_id` present on all trips,
  - quoted-comma `service_id` values present.

## Legal/Freshness Status

- Real Rakvere production bundled asset is not automatically approved.
- Real asset generation is constrained until legal/source/freshness policy is explicitly documented and accepted.
- This pass does not claim legal certainty and does not add legal conclusions.
- Raw `rakvere.zip` remains forbidden from repository commits.

## Technical Profile Summary

- Planning profile documented in `docs/GTFS_PIPELINE.md`:
  - 4 routes / 361 trips / 1215 stops / 6292 `stop_times`,
  - 98 stops with `stop_area = "Rakvere linn"`,
  - no blank arrival/departure rows,
  - no times over `24:00:00` in current snapshot.

## MVP Field Policy

- MVP-use, tolerate, and future-only GTFS fields are documented in `docs/GTFS_PIPELINE.md`.
- Policy includes explicit treatment for:
  - `stop_area` as Rakvere filtering candidate,
  - `block_id` tolerated but routing-inactive,
  - unknown columns tolerated and ignored,
  - Google Transit extension fields non-blocking for MVP.

## Parser Robustness Requirements

- Unknown column tolerance.
- Correct CSV quoted-comma parsing for `service_id`.
- Correct `calendar_dates` exception handling.
- No hard requirement for `attributions.txt`, `frequencies.txt`, `transfers.txt`.
- `StopPointId` preservation from `stop_id` only; never from display names.
- Explicit and tested Rakvere `stop_area` filtering policy before real asset commit.

## Source-Code Untouched Confirmation

- No Kotlin/source files changed.
- No parser/Room/app/feature runtime code changed.
- No build/CI/lockfile changes.
- No real asset generation and no ZIP commit.

## Validation Result

Executed:

- `py -3 tools/validate_project_state.py`
- `git diff --check`
- `git status --short --untracked-files=all`
- `git diff --name-only`

All passed for docs-only scope.

## Next Recommended Pass

- `PASS 26A — REAL_RAKVERE_FEED_PROFILE_AND_PARSER_ROBUSTNESS_TESTS`
- Then `PASS 26B — REAL_RAKVERE_BUNDLED_FEED_ASSET_GENERATION` only after legal/source/freshness policy acceptance.
