## 1. Model

- [x] 1.1 `RepoConfig` with id, URL, label and a `hasToken` flag
- [x] 1.2 `RepoList` with pure `add`, `remove`, `activate` and `withToken`
- [x] 1.3 `repoIdFor(url)` deriving a stable id, and `labelFor(url)`

## 2. Registry

- [x] 2.1 `RepoRegistry` over a preferences DataStore holding the list as JSON
- [x] 2.2 Observable `repos` flow and a `current()` snapshot
- [x] 2.3 `add`, `remove`, `activate`, `tokenFor`
- [x] 2.4 Removing a repository removes its token

## 3. Token vault

- [x] 3.1 `TokenVault` interface plus an in-memory implementation for tests
- [x] 3.2 `KeystoreTokenVault`: AES-256 GCM with a key generated in
      `AndroidKeyStore`, storing iv and ciphertext base64-encoded
- [x] 3.3 Decryption failure yields null rather than throwing

## 4. Dependencies

- [x] 4.1 Add `datastore-preferences` and `kotlinx-serialization-json` to the
      catalog, and the serialization plugin

## 5. Tests

- [x] 5.1 Unit: first repository becomes active, switching, removing the active
      one, removing the last one
- [x] 5.2 Unit: adding the same URL twice keeps one entry and updates the label
- [x] 5.3 Unit: id is stable and case-insensitive, label derived from the URL
- [x] 5.4 Unit: rendering a config does not expose a token
- [x] 5.5 Instrumented: token round-trips through the keystore
- [x] 5.6 Instrumented: the persisted file does not contain the plain token
- [x] 5.7 Instrumented: tokens for two repositories stay independent
- [x] 5.8 Instrumented: the list survives reopening the same file
- [x] 5.9 Instrumented: removing a repository removes its token and leaves the
      other one alone
