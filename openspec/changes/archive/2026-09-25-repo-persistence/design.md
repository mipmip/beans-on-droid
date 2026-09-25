## Context

Two different persistence problems share a change because they share a lifecycle:
the token exists exactly as long as the repository it belongs to.

## Decisions

### The list is one JSON document in DataStore, not a row per field

`RepoList` (the repositories plus which is active) is serialised whole with
kotlinx-serialization and written to a single preferences key. The alternative,
a key per repository per field, makes "add" and "remove" multi-write operations
that can half-fail. One document means a change is one atomic edit, and the
active-repository invariant is enforced in one place.

### The list logic is pure and the storage is thin

`RepoList.add`, `remove`, `activate` and `withToken` are pure functions on an
immutable value. `RepoRegistry` only decodes, applies one of them, and encodes.
That is why the invariants (first repository becomes active, removing the active
one promotes another, the same URL is not added twice) are covered by fast unit
tests with no Android at all, and the instrumented tests only have to prove that
DataStore writes and reads back.

### The repository id is derived from the URL

A hash of the trimmed, lowercased URL, in base 36. Adding the same repository
twice is then idempotent without a lookup, and the id is stable across
reinstalls, which keeps the working directory name and the token key aligned
with the list entry. A random UUID would need the duplicate check to happen
before the id exists.

### The token is encrypted with a Keystore key, not with a library

`KeystoreTokenVault` generates an AES-256 key in `AndroidKeyStore` and encrypts
with AES/GCM, storing `iv || ciphertext` base64-encoded. The key never leaves the
keystore, so the stored bytes are useless without the device.

`androidx.security:security-crypto` would do the same thing with less code, but
it has been deprecated, and it hides which key is used and where. Twelve lines of
`Cipher` with a named transformation is something a reader can audit, which
matters more for the one credential this app holds than brevity does.

Randomised encryption is required on the key, so every write gets a fresh IV and
two writes of the same token produce different ciphertext.

### `TokenVault` is an interface

The registry depends on the interface, so its tests use `InMemoryTokenVault` and
never touch the keystore. The Keystore implementation is then tested on its own,
on a device, against the one property that matters: the plain token is not in the
file.

### `hasToken` is a flag, never the token

`RepoConfig` records whether a token exists so the interface can show a lock
without reading a secret to render a list. The token itself is only fetched when
a clone or fetch is about to use it. The generated `toString` therefore cannot
leak one, which is what the diagnostics requirement asks for.

## Verification

The invariants are unit tested. The two things only a device can prove are
instrumented: that a token written through the keystore comes back, and that the
bytes on disk do not contain it. That second test reads the preferences file and
searches for the plaintext.

## Risks

- A keystore key can be invalidated by a device event such as a biometric reset,
  and then a stored token no longer decrypts. `get` returns null rather than
  throwing, so the app behaves as though no token were stored and the user can
  enter it again. Nothing is lost except the token.
