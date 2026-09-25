## What review turned up

The three screens each had an empty state, so this epic looked like a rubber
stamp. Walking the states against the briefing instead of against the code found
three places where the app asserts something false.

### A failed refresh threw away good data

`refresh()` set the state to `Failed` on any error. So pulling to refresh on a
train, with a repository already cloned and indexed, replaced a perfectly usable
list of beans with an error page. The data had not gone anywhere; only the
attempt to update it had failed.

`IndexState.Ready` now carries a nullable `staleReason`. A refresh that fails
while that repository's beans are showing keeps the `Ready` state and sets the
reason. A refresh that fails with nothing to fall back on still fails. The list
shows a banner naming the reason with a retry, and the beans stay.

### Recreating a screen cleared the banner

`AppViewModel.init` called `loadActive()`, which reindexes unconditionally. So
rotating the phone after a failed refresh produced a fresh `Ready` with no stale
reason: the app quietly dropped the warning and presented stale data as current.

`loadActiveIfNeeded()` returns early when the state is already `Ready` for the
active repository. That also removes a pointless reindex on every configuration
change.

### The detail screen claimed beans were missing

It read `indexState as? IndexState.Ready`, and on null said "that bean is not
here". That is only true when the index is loaded and the bean is not in it. With
no repository open, or during a clone, or after a failure, the app was blaming
the bean for the app's own state. The two cases are now separate.

### The skipped count named nothing

The parser skips a malformed file rather than failing the repository, and the
list showed how many. A count with no names is not actionable: the user cannot
tell which file to go and fix. Tapping it now lists each filename with the
parser's reason.

## Decisions

### Stale is a property of `Ready`, not a fourth state

Modelling "showing beans, but they may be out of date" as a flag on `Ready`
rather than as its own state means every screen that already handles `Ready`
keeps working, and only the list has to know about the banner. A fifth state
would have forced every `when` to grow a branch that mostly duplicates `Ready`.

### The banner is in the error container colour, not a toast

A toast disappears and is then indistinguishable from never having happened.
Stale data is a condition, not an event, so it gets a persistent banner that
stays until a refresh succeeds.

## Verification

Seven instrumented tests covering the states across both screens: no repository,
a repository with no beans, a missing bean directory, an unreachable repository,
unreadable files counted and then listed by name, a failed refresh keeping its
beans with a retry, and the detail screen not blaming a missing bean.

Five unit tests cover the transitions underneath: a failed refresh keeping
`Ready` with a reason, a later success clearing it, a failure with nothing to
fall back on still failing, and `loadActiveIfNeeded` both preserving a stale
marker and loading when there is nothing.

One fixture bug was found on the way: a test repository with an empty `.beans`
directory clones without it, because git does not track empty directories. The
fixture now writes a `.gitkeep`, which is what a real repository in that state
would have.
