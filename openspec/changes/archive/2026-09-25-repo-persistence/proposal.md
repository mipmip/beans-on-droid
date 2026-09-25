## Why

Bean `beans-on-droid-rs1v`. `RepoStore` can clone a repository but the app
forgets it the moment the process dies, and there is nowhere to put the token a
private repository needs. A token is the most sensitive thing this app will ever
hold: it is a credential to someone's source code, so where it is kept is a
design decision, not an implementation detail.

## What Changes

- Add `RepoConfig`: the identity, URL and label of a repository the user added,
  plus whether it is the active one.
- Add `RepoRegistry` over DataStore: observe the repository list, add, remove and
  switch the active repository.
- Add `TokenVault`: store, read and remove a per-repository token, encrypted
  with an AES-GCM key held in the Android Keystore. The plaintext token never
  reaches disk.
- Removing a repository removes its token and its working copy together.
- Keep tokens out of logs and out of `toString`.

## Capabilities

### New Capabilities

- `repo-persistence`: remembering which repositories the user added, which one
  is active, and holding their access tokens safely.

### Modified Capabilities

None.

## Impact

- New: `store/RepoConfig.kt`, `store/RepoRegistry.kt`, `store/TokenVault.kt`,
  `store/KeystoreTokenVault.kt` and tests.
- New dependencies: `androidx.datastore:datastore-preferences` and
  `kotlinx-serialization-json`, both Apache-2.0.
