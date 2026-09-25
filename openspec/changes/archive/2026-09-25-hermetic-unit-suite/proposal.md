## Why

Bean `beans-on-droid-xhwh`. The unit suite is thorough but it was not
self-contained: four tests proved the network failure path by relying on
`beans.invalid` not resolving, which makes them dependent on the DNS the machine
happens to be using. A suite that can pass or fail based on a captive portal is
not a suite anyone can trust a gate to.

The same review found a test double, `InMemoryTokenVault`, living in production
code so that instrumented tests could reach it.

## What Changes

- Replace the unresolvable hostname with a closed port on the loopback address,
  so the network failure path is exercised without leaving the machine.
- Remove `InMemoryTokenVault` from production code and give the instrumented
  tests their own fake.
- Record the suite's figures and prove the suite runs with no network at all.

## Capabilities

### New Capabilities

None. Test and structural work, so `skip_specs: true`.

### Modified Capabilities

None.

## Impact

- Modified: three unit test files, `store/TokenVault.kt`, two instrumented test
  files.
- The unit suite no longer touches the network.
