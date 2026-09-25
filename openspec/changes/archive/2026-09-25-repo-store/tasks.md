## 1. Dependency

- [x] 1.1 Add JGit and Kotlin coroutines to the version catalog and the app module
- [x] 1.2 Confirm the transitive set is FOSS and small

## 2. Store

- [x] 2.1 `RepoError` and `RepoResult` with the four failure categories
- [x] 2.2 `RepoStore.clone` with depth 1, optional token, into a per-repository
      directory
- [x] 2.3 `RepoStore.refresh` as fetch plus hard reset to the remote branch
- [x] 2.4 `RepoStore.delete`
- [x] 2.5 Delete the target directory before cloning and after a failed clone
- [x] 2.6 Classify failures by walking the cause chain

## 3. Beans layout

- [x] 3.1 Read the bean path from `.beans.yml`, falling back to `.beans`
- [x] 3.2 Report a repository with neither as not-a-beans-repository
- [x] 3.3 List bean files including the archive directory, marking which are
      archived

## 4. Android adaptation

- [x] 4.1 `AndroidGit.install` replacing JGit's `SystemReader`
- [x] 4.2 Application class installing it at startup

## 5. Tests

- [x] 5.1 Clone produces a working copy with the repository's files
- [x] 5.2 Clone fetches a single commit of history
- [x] 5.3 Two repositories do not collide
- [x] 5.4 Refresh picks up a new commit
- [x] 5.5 Refresh discards a local modification
- [x] 5.6 Refresh follows a rewritten remote
- [x] 5.7 Delete removes only the named repository
- [x] 5.8 Unreachable host is classified as a network failure
- [x] 5.9 A failed clone leaves no directory behind
- [x] 5.10 Bean directory found from config, from default, and reported missing
- [x] 5.11 Instrumented: JGit clones, refreshes and parses on an API 26 emulator

## 6. Emulator

- [x] 6.1 Add an `emulator` dev shell to the flake carrying the emulator and an
      API 26 AOSP x86_64 system image
- [x] 6.2 Add `scripts/emulator.sh` to create and boot the AVD headless
- [x] 6.3 Establish which JGit version actually runs on API 26 and record the
      evidence in the design
