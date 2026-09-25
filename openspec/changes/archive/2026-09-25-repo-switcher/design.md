## Decisions

### The dialog is the form, and it owns nothing

The add-repository form's state lives in `AppViewModel`, not in the composable.
A clone survives a configuration change that way, and the dialog can be reduced
to parameters and callbacks. That is also what makes the instrumented tests able
to assert on the state rather than on pixels.

### Removal is confirmed because it is not reversible

Removing a repository deletes the working copy and the token. Neither can be
recovered from the app, and the token in particular may not be recoverable at
all if the user did not save it elsewhere. The confirmation says exactly what is
deleted, and says that the server is untouched, because "remove repository" is
the kind of phrase that can read as "delete my repository".

### The token field is masked and never read back

`PasswordVisualTransformation` on input, and the list shows a lock icon rather
than any part of the value. `RepoConfig.hasToken` is what the list binds to, so
rendering the screen never fetches a secret.

### Content descriptions are part of the design, not an afterthought

Every control the tests drive has a content description, which is the same thing
a screen reader announces. Writing the tests against those descriptions means an
unlabelled control fails a test rather than quietly shipping.

## Verification

Five instrumented Compose tests on an API 26 emulator: the empty state, an SSH
URL rejected without a repository being created, a real clone from a local
repository appearing in the list and becoming active, cancel-then-confirm on
removal, and the token marker.

The clone in these tests is a genuine JGit clone of a repository created by the
test, so "adding a repository works" is asserted end to end rather than mocked.
