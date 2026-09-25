## The defect

The second violation rule was written as:

```kotlin
rule {
    element = "BUNDLE"
    limit { ... minimum = "0.80".toBigDecimal() }
    classDirectories.setFrom(fileTree(...) { include(corePackages) })
}
```

`JacocoViolationRule` has no `classDirectories` property. Inside the `rule { }`
lambda, Kotlin resolved `classDirectories` against the next receiver out, which
is the `JacocoCoverageVerification` task. So the line did not scope one rule to
the core packages; it replaced the entire task's class set with the core packages
for every rule.

Both rules were therefore evaluated against the bean and index packages alone,
which sit at 91 percent. The 70 percent bundle rule was being applied to the same
two packages it was meant to be a wider check on, and the rest of the app was
never measured at all. It compiled, it ran, and it reported success.

## The fix

JaCoCo scopes a rule with `element` and `includes`, matching on the element's
name:

```kotlin
rule {
    element = "PACKAGE"
    includes = listOf(
        "io.github.mipmip.beansondroid.bean",
        "io.github.mipmip.beansondroid.index",
    )
    limit { ... minimum = "0.80".toBigDecimal() }
}
```

The task's `classDirectories` is then set once, for the whole measured bundle,
and the rules differ only in what they select from it.

## What is excluded, and why

A unit test coverage report cannot see instrumented tests: they run in a
different process on a device and produce no data in this report. Classes that
exist only to talk to the Android framework are therefore uncoverable here no
matter how well tested they are, and leaving them in the bundle would force the
floor down until it stopped meaning anything.

Excluded, in addition to the generated classes and the Compose UI already
excluded:

| Class                   | Covered instead by                                |
|-------------------------|---------------------------------------------------|
| `BeansOnDroidApplication` | Nothing; it is three lines of wiring             |
| `AndroidGit`, `AndroidSystemReader` | `JGitRuntimeTest` on an API 26 emulator |
| `RepoRegistry`          | `PersistenceTest` on a device                     |
| `KeystoreTokenVault`    | `PersistenceTest`, including the plaintext check  |

The logic these classes delegate to stays measured. `RepoRegistry` is a thin
wrapper over `RepoList`, whose `add`, `remove`, `activate` and `withToken` are
pure and unit tested; excluding the wrapper does not excuse the rules it applies.

This is a judgement call and it is the second time this project has made it, so
it is worth naming the line: a class is excluded when running it requires a
device, and not when testing it would merely be inconvenient.

## Verification

The fix is only believable if the gate is shown to fail, so both rules were made
to fail on purpose.

Bundle floor raised to 0.90:

```
Rule violated for bundle app: instructions covered ratio is 0.84,
but expected minimum is 0.90
```

Core floor raised to 0.95:

```
Rule violated for package io.github.mipmip.beansondroid.bean:
  instructions covered ratio is 0.90, but expected minimum is 0.95
Rule violated for package io.github.mipmip.beansondroid.index:
  instructions covered ratio is 0.92, but expected minimum is 0.95
```

The bundle figure is now 0.84 rather than the 0.91 the broken configuration
reported, which is the measurable proof that the whole app is being counted. The
core rule names the packages it checked, which the previous one could not do.

With the floors restored, overall instruction coverage is 84.2 percent: bean
90.9, index 92.9, repo 83.3, store 58.1.
