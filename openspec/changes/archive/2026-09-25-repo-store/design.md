## Context

This is the layer with an external dependency that has to work on a platform it
was not written for. JGit targets desktop JVMs; Android is a JVM with no
`/etc/gitconfig`, no writable home directory and no `git` binary to shell out to.

## Decisions

### JGit 6.4.0, found by running it rather than by reading version numbers

The briefing warned that JGit might not run on API 26 even when it compiles. It
does not, and finding a version that does took an experiment.

**JGit 6.10.1 compiles, builds, installs and then dies on the first git
operation:**

```
java.lang.NoSuchMethodError: No virtual method readNBytes(I)[B
  at org.eclipse.jgit.util.IO.readFully(IO.java:90)
  at org.eclipse.jgit.storage.file.FileBasedConfig.load(FileBasedConfig.java:148)
```

`InputStream.readNBytes` arrived in Java 9 and on Android only in API 33. It is
on the path that reads a repository's `.git/config`, so every operation hits it.

**Core library desugaring does not fix it.** Switching
`desugar_jdk_libs` for `desugar_jdk_libs_nio` changed nothing: D8 can backport
static methods and can desugar `java.nio.file`, but it cannot add an instance
method to the platform's `java.io.InputStream`.

**JGit 5.13.5, the last Java 8 baseline release, has no shallow clone.**
`javap` on `CloneCommand` and `FetchCommand` shows no `setDepth` in 5.13.x; that
support arrived in the 6.x line. Using it would mean giving up depth 1, which
the briefing puts in scope.

**So the search was for the newest 6.x that does not call `readNBytes`.**
Scanning the jars:

| Version | `setDepth` | files calling `readNBytes` |
|---------|------------|----------------------------|
| 6.3.0   | yes        | 0                          |
| 6.4.0   | yes        | 0                          |
| 6.5.0   | yes        | 3 (not `IO`)               |
| 6.7.0   | yes        | 4, including `IO`          |
| 6.10.1  | yes        | 5, including `IO`          |

6.4.0 is the newest release with shallow clone and no `readNBytes`. It was then
verified by running it: the instrumented tests clone, refresh and parse a
repository on an API 26 emulator, and they pass. Compiling was never the
question.

`desugar_jdk_libs_nio` is kept, because JGit 6.4.0 does use `Files.readString`
and `Files.writeString`, which are Java 11 and which the nio variant does cover.

JGit is licensed under the Eclipse Distribution License, a BSD-3 variant, which
is FOSS and compatible with Apache-2.0 distribution.

### A custom `SystemReader` is installed before any git operation

JGit asks a `SystemReader` for the user and system git config and for the home
directory. On Android the defaults point at paths the app cannot read or write,
and the failure is an opaque exception deep inside a clone.

`AndroidGit.install(dir)` replaces it with one that reports the app's private
directory as the home, and opens the user, system and jgit configs as files
inside that directory. The application class calls it once at startup, and the
instrumented tests call it themselves so they do not depend on the application
having been created.

### `RepoStore` takes a directory, not a `Context`

The store is constructed with a root `File`. That is the whole reason its
behavior can be unit tested on the JVM against a real local repository, with no
Robolectric and no emulator. The Android-specific part is one line at the call
site passing `filesDir`.

### Authentication is the token as the username

GitHub, GitLab and Gitea all accept a personal access token as the HTTP basic
username with an empty or ignored password. Putting the token in the username
field works across all three, which matches the briefing's requirement to stay
host-agnostic. No token means no credentials provider at all, so a public clone
does not send an empty authorization header.

### Refresh resolves the remote tip by name

After a shallow fetch the store resets hard to
`refs/remotes/origin/<current branch>`, falling back to `refs/remotes/origin/HEAD`.
Resetting to a ref rather than to `FETCH_HEAD` is what makes a force-pushed
remote work: the local branch simply points somewhere else afterwards, and there
is never a merge to fail.

### Errors are classified by inspecting the whole cause chain

JGit reports an authentication failure and a DNS failure as the same
`TransportException` type, with the detail in the message or a nested cause. The
classifier walks the cause chain, collects the text, and matches known markers.
It is inelegant and it is the only thing that works without depending on JGit
internals. `UnknownHostException` anywhere in the chain is treated as a network
failure regardless of wording.

### A failed clone deletes its directory

JGit leaves a partial working copy behind when a clone fails part way. The store
removes the target directory both before cloning and after a failure, so a retry
starts clean and a failed add never leaves a repository that looks present but
is not.

## Verification

Unit tests run against a local repository created by JGit in a temporary
directory, so the suite needs no network and no fixtures checked in. Two cases
do reach for a hostname that cannot resolve, which exercises the network
classification without depending on any host being up.

The on-device claim is tested separately, by instrumented tests that clone,
refresh and parse on an API 26 emulator. Compiling is not evidence that JGit
runs, which is why those tests exist.

## Risks

- JGit 6.4.0 is pinned for a reason, and that reason is not visible in the
  version number. Anyone bumping it must re-run the instrumented tests on an
  API 26 emulator, not just the unit tests. The table above is the record of
  what breaks and when.
- The error classifier matches on message text, so a JGit upgrade could reword a
  message and downgrade a specific error to `Unknown`. The user still sees the
  message; only the category is lost. Worth rechecking on any JGit bump.
