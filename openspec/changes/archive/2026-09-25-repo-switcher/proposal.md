## Why

Bean `beans-on-droid-khl2`. Adding a repository is the first thing anyone does
with this app and currently there is no way to do it. It is also where the two
things most likely to go wrong live: a mistyped URL and a token that does not
work. This screen has to make both legible.

## What Changes

- Replace the repository stub with a real screen: the list of repositories, which
  one is active, and which carry a token.
- Add a form for a clone URL, an optional name and an optional access token, with
  the token field masked.
- Validate the URL before any network call and show the reason inline.
- Show that a clone is in progress and keep the form locked while it runs.
- Confirm before removing a repository, and say what removal actually deletes.
- Tapping a repository makes it active, which reindexes.

## Capabilities

### New Capabilities

- `repo-switcher`: managing the set of repositories on the device and choosing
  which one is being read.

### Modified Capabilities

None.

## Impact

- Modified: `ui/screen/RepoScreen.kt`.
- New test helpers shared by later UI tests.
- No new dependencies.
