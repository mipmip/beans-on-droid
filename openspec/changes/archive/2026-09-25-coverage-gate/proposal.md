## Why

Bean `beans-on-droid-f006`. `scripts/ship-change.sh` refuses to archive a change
when the gate fails, but the gate currently only proves the app compiles. The
briefing's definition of done requires a coverage floor, and a floor that is not
enforced is a suggestion. Adding it now means every later change has to arrive
with its tests, instead of a test debt being discovered at the end.

## What Changes

- Apply the Jacoco plugin to `:app` and add a `jacocoTestReport` task that covers
  the debug unit test run, reading both Java and Kotlin class output.
- Add `jacocoCoverageVerification` with two rules: 70 percent instruction
  coverage over the whole measured bundle, and 80 percent over the `bean` and
  `index` packages.
- Exclude generated Android classes and the Compose UI layer from the measured
  bundle, and state why.
- Put `jacocoCoverageVerification` back into `scripts/gate.sh`.

## Capabilities

### New Capabilities

None. Build tooling with no user-observable behavior, so `skip_specs: true`.

### Modified Capabilities

None.

## Impact

- Modified: `app/build.gradle.kts`, `gradle/libs.versions.toml`,
  `scripts/gate.sh`.
- From here on every change that adds logic must arrive with tests, or the ship
  aborts before archiving.
