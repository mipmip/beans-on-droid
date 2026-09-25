## Context

The briefing asks for 70 percent overall and 80 percent on core packages. Those
numbers need a definition of what is measured before they mean anything.

## Decisions

### What is measured

The measured bundle is the app's own logic: the bean model, the parser, the
index, the repository store and the view models. Excluded:

- Generated Android classes (`R`, `BuildConfig`, `*_Factory`, manifest-generated
  classes). They contain no authored logic.
- `io/github/mipmip/beansondroid/ui/**` and `MainActivity`. Composable functions
  and the activity are exercised by instrumented tests on a device, which run in
  a different JVM and do not contribute to the unit test Jacoco report. Counting
  them in a unit test coverage number would measure nothing and would force the
  floor to be set so low it stops being a gate.

So "70 percent overall" means 70 percent of the non-UI, non-generated code. This
is a deliberate reading of the briefing, recorded here so the number is not
mistaken for whole-app coverage. The UI is covered instead by the instrumented
end-to-end tests in `beans-on-droid-q6t9`.

### Two rules, not one

A single bundle-wide rule lets a large well-tested area hide a weak parser. The
second rule scopes 80 percent to the packages that must not be wrong: the bean
model and parser, and the index.

### Instruction counter

Instruction coverage rather than line coverage, because Kotlin's generated
bridge code and default arguments make line counts noisy across compiler
versions.

### Empty bundles pass

Jacoco skips a ratio rule when the denominator is zero. Until the first
production class exists, the gate passes trivially. That is correct: the gate
exists to stop untested code from shipping, not to block an empty project.

## Risks

- Excluding the UI means a Compose regression cannot be caught by the unit gate.
  Mitigated by the instrumented tests, which the gate will also run once they
  exist.
