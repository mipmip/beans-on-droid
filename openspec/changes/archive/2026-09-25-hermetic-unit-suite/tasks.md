## 1. Hermetic

- [x] 1.1 Replace the unresolvable hostname with a closed loopback port in all
      four tests
- [x] 1.2 Confirm the failure is still classified as a network failure
- [x] 1.3 Run the whole suite in a network namespace with only loopback

## 2. Structure

- [x] 2.1 Remove `InMemoryTokenVault` from production code
- [x] 2.2 Add `FakeTokenVault` to the instrumented test source set
- [x] 2.3 No references to the old class remain

## 3. Figures

- [x] 3.1 Record per-package coverage and the total
- [x] 3.2 Confirm `./gradlew test` passes and the coverage gate holds
