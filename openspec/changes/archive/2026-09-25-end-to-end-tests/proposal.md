## Why

Bean `beans-on-droid-q6t9`. Every layer is tested and every screen is tested,
which is not the same as the app working. Nothing yet drives the whole thing from
an empty install to reading a bean, and nothing at all exercises HTTP: the
existing tests clone from `file://`, so the transport the app actually uses on a
phone has never run in a test.

## What Changes

- Add a git server for the tests that speaks the smart HTTP protocol on
  loopback, with optional HTTP basic auth.
- Add end-to-end tests driving the real navigation graph: add a repository over
  HTTP, land on a populated list, search and filter, open a bean and follow a
  relationship, pull to refresh and see a new commit, and fail with a bad token.
- Add `scripts/e2e.sh`, which boots a headless emulator, runs the instrumented
  tests and shuts it down.
- Add a data-layer test proving shallow clone and refresh work over HTTP, not
  only over `file://`.

## Capabilities

### New Capabilities

- `end-to-end`: the flows a person actually performs, exercised against a real
  git server over HTTP.

### Modified Capabilities

None.

## Impact

- New: `GitHttpServer`, `EndToEndTest`, `scripts/e2e.sh`.
- Modified: a test tag on the bean list so a pull gesture can target it.
- No new production dependencies.
