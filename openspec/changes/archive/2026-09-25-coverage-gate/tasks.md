## 1. Jacoco configuration

- [x] 1.1 Add the Jacoco tool version to `gradle/libs.versions.toml`
- [x] 1.2 Apply the `jacoco` plugin to `:app` and pin the tool version
- [x] 1.3 Make the debug unit test task produce execution data
- [x] 1.4 Add `jacocoTestReport` reading Kotlin and Java class output plus the
      main source sets, with the exclusion list applied
- [x] 1.5 Add `jacocoCoverageVerification` with a 70 percent bundle rule and an
      80 percent rule scoped to the bean and index packages
- [x] 1.6 Make `jacocoCoverageVerification` depend on the test run

## 2. Gate

- [x] 2.1 Add `jacocoCoverageVerification` back to `scripts/gate.sh`
- [x] 2.2 `./scripts/gate.sh` passes on the current tree

## 3. Verification

- [x] 3.1 `nix develop -c ./gradlew jacocoTestReport` writes an XML and an HTML
      report
- [x] 3.2 A deliberately uncovered class in a measured package fails
      `jacocoCoverageVerification`, proving the gate bites
