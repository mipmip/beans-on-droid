## Why

Bean `beans-on-droid-43ff`. The README still says the project is scaffolded and
the app is being built. It is built. More importantly, the JGit constraint that
took the longest to find is recorded only in an archived OpenSpec change, where
the next person to bump a dependency will not look.

## What Changes

- Rewrite the README: what the app is and that it is unofficial, how to add a
  repository and a token, how to build with and without nix, and what the checks
  are.
- Document the JGit version constraint and the minSdk 26 workaround in the
  README, with the evidence, because that is where someone about to bump it will
  be.
- Describe the layering and point at the format, performance and F-Droid
  documents.
- State the Phase 2 roadmap and what is explicitly not planned.

## Capabilities

### New Capabilities

None. Documentation, so `skip_specs: true`.

### Modified Capabilities

None.

## Impact

- Modified: `README.md`.
