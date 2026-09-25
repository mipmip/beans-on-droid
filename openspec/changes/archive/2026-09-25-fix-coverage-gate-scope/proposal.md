## Why

The coverage gate shipped in `beans-on-droid-f006` does not measure what it
claims to. Overall instruction coverage is 66.5 percent, below the 70 percent
floor, and `jacocoCoverageVerification` passes. A gate that reports green while
the floor is breached is worse than no gate, because every change since has been
shipped on the strength of it.

## What Changes

- Scope the 80 percent core rule with `element = "PACKAGE"` and `includes`,
  instead of overriding `classDirectories` inside the rule.
- Exclude from the measured bundle the classes that can only run on a device,
  and say why: they are covered by instrumented tests, which a unit test
  coverage report cannot see.
- Verify the gate fails when the floor is breached, rather than assuming it.

## Capabilities

### New Capabilities

None. Build tooling, so `skip_specs: true`.

### Modified Capabilities

None.

## Impact

- Modified: `app/build.gradle.kts`.
- The gate starts enforcing the floor, which it has not been doing.
