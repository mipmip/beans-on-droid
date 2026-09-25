## Why

Bean `beans-on-droid-ik0p`. The briefing requires the app to handle a few
hundred beans without noticeable lag, and several design decisions were taken on
the assumption that it does: search runs unthrottled on every keystroke, the
index is a linear scan, and the whole index is held in memory. None of that has
been measured.

## What Changes

- Add a performance test that generates a repository of 600 realistic beans and
  times parsing, indexing, searching, filtering, relationship lookups, scrolling
  and the search-to-result round trip.
- Assert bounds on each, so a regression fails rather than being noticed later.
- Record the numbers, the method and the point at which the design stops
  holding, in `docs/performance.md`.

## Capabilities

### New Capabilities

None. Measurement, so `skip_specs: true`.

### Modified Capabilities

None.

## Impact

- New: `PerformanceTest`, `docs/performance.md`.
