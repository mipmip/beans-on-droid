## 1. Make the version reachable

- [x] 1.1 Enable `buildConfig` generation
- [x] 1.2 Confirm `BuildConfig.VERSION_NAME` and `VERSION_CODE` carry the values
      derived from `VERSION`

## 2. Show it

- [x] 2.1 Footer on the repository screen with the name and code
- [x] 2.2 Visible with repositories configured and with none
- [x] 2.3 A content description so it is reachable by a screen reader

## 3. Tests

- [x] 3.1 Instrumented: the version is shown when repositories exist
- [x] 3.2 Instrumented: the version is shown when the list is empty
- [x] 3.3 Instrumented: the shown values match `BuildConfig`

## 4. Verification

- [x] 4.1 `./scripts/gate.sh` passes
- [x] 4.2 The instrumented suite passes
- [x] 4.3 Visible on a real device
