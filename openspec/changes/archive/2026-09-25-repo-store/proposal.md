## Why

Bean `beans-on-droid-c85j`. The parser and index can read beans but nothing puts
any on the device. This change adds the layer that clones a repository over
HTTPS, refreshes it, deletes it, and reports failures in terms a user can act on.
It is deliberately read-only: the app never writes to a repository, which is what
lets refresh be a hard reset instead of a merge.

## What Changes

- Add `RepoStore`: clone, refresh, delete, and expose the working directory and
  the bean directory for a repository.
- Shallow clone at depth 1 into app-private storage, with optional token
  authentication over HTTPS.
- Refresh as fetch plus hard reset to the tracked remote branch.
- Locate the bean directory by reading `.beans.yml`, falling back to `.beans`.
- Classify failures as authentication, network, not-a-beans-repository or
  unknown, each carrying a message.
- Add JGit, and whatever Android adaptation it turns out to need.

## Capabilities

### New Capabilities

- `repo-storage`: obtaining and refreshing a local read-only copy of a remote
  git repository, and locating the beans inside it.

### Modified Capabilities

None.

## Impact

- New: `repo/RepoStore.kt`, `repo/RepoError.kt`, `repo/BeansLayout.kt` and tests.
- New dependency: JGit (Eclipse Distribution License, FOSS).
- Tests run against a local bare repository, never against the network.
