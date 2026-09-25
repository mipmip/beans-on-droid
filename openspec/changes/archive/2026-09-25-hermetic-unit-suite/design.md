## A test double does not belong in production code

`InMemoryTokenVault` sat next to the `TokenVault` interface in `main` because the
instrumented tests needed it and cannot see the unit test source set. That put a
class whose only purpose is testing into the shipped APK, and it dragged the
`store` package's coverage down with code no unit test has any reason to run.

It is now a `FakeTokenVault` in the instrumented test source set, where its one
consumer is. The alternative, a `testFixtures` source set, would be the right
answer if a third source set needed it; two do not justify the configuration.

## Proving the suite is hermetic

Changing `https://beans.invalid/...` to `http://127.0.0.1:1/...` makes the
failure a connection refused on a closed port instead of a DNS lookup that is
expected to fail. That is a stronger test as well as a self-contained one: DNS
failure and connection refusal take different paths through the error
classifier, and connection refusal is what a user on a bad network actually
gets.

The claim was then checked rather than asserted. The whole suite runs inside a
network namespace with no interface except loopback:

```
unshare -r -n bash -c "ip link set lo up; ./gradlew testDebugUnitTest \
  --rerun-tasks --offline"
BUILD SUCCESSFUL
```

Loopback has to be up because Gradle itself needs it to start. Nothing can leave
the machine, and 119 tests pass.

## The figures

| Package     | Instruction coverage |
|-------------|----------------------|
| `bean`      | 90.9 percent         |
| `index`     | 92.9 percent         |
| `data`      | 97.1 percent         |
| `viewmodel` | 97.2 percent         |
| `repo`      | 84.6 percent         |
| `store`     | 66.8 percent         |
| **Overall** | **89.3 percent**     |

Against floors of 70 percent overall and 80 percent on `bean` and `index`.

`store` is the lowest because its two Android-bound classes are excluded from the
measured bundle and covered by instrumented tests instead; what remains measured
is `RepoList`, `RepoUrl` and the data classes, and the uncovered part is the
generated `copy`/`equals` surface of the serialisable types.

## What the suite covers

119 unit tests: the parser against real fixtures from `hmans/beans` and synthetic
edge cases, the index's filters, search and relationship resolution, the store
against local git repositories including a rewritten remote, the coordinator's
state machine, and the view model's transitions driven by a test dispatcher.

41 instrumented tests on an API 26 emulator cover what a JVM cannot: JGit
actually running, the keystore, DataStore, and all three screens.
