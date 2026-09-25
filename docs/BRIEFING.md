# Briefing: Beans on Droid, Phase 1 (read-only)

An unofficial Android app for browsing and reading issues ("beans") from the
flat-file issue tracker [hmans/beans](https://github.com/hmans/beans).

This document is the source of truth for what Phase 1 is. OpenSpec proposals
refine it; they do not replace it.

## Fixed settings

| Setting        | Value                             |
|----------------|-----------------------------------|
| Application ID | `io.github.mipmip.beansondroid`   |
| App name       | Beans on Droid                    |
| License        | Apache-2.0                        |
| minSdk         | 26                                |
| versionCode    | 1                                 |
| versionName    | 0.1.0                             |

The Application ID is permanent once published.

## Background

Beans stores issues as Markdown files with YAML frontmatter in a `.beans/`
directory at the project root, configured by `.beans.yml`. Everything lives in
the git repo next to the code.

**First step:** inspect the real format before writing the parser. Clone
`https://github.com/hmans/beans` (it tracks its own issues in `.beans/`), read
several bean files and `.beans.yml`, and derive the model from them. Consult the
Go source in that repo if a field's meaning is unclear. Do not guess the schema
from this briefing.

## Scope: Phase 1

- Add one or more repos by HTTPS URL, with an optional personal access token for
  private repos.
- Shallow clone (`depth 1`) into app-private storage.
- Refresh via pull-to-refresh. Refresh is a fetch plus a hard reset to the remote
  branch, because the app never writes, so it never merges.
- Parse all beans into an in-memory index on each clone or refresh.
- **List screen:** all beans, with filters for status, type, and tags, plus a text
  search on title and body. Show ID, title, status, and type.
- **Detail screen:** frontmatter fields, the Markdown body rendered, and tappable
  links to related beans (parent, children, blocking, and so on, whatever the
  format supports).
- **Repo switcher:** add, remove, and switch between repos.
- Readable empty, loading, and error states (auth failure, network error, no
  `.beans/` dir).

## Non-goals (Phase 1)

- No editing, committing, or pushing.
- No SSH authentication.
- No GitHub API usage. Stay host-agnostic, since plain git is the data layer.
- No background sync or notifications.

## Tech stack

- Kotlin, Jetpack Compose, Material 3, single activity, Navigation Compose.
- Current stable versions of AGP, Kotlin, and Compose BOM, with a Gradle version
  catalog (`gradle/libs.versions.toml`).
- **JGit** for clone and fetch. Check compatibility with minSdk 26. Newer JGit
  needs Java 11+ APIs, so enable core library desugaring or use a JGit version
  that works on Android. Verify that it actually runs, not just that it compiles.
- A FOSS Markdown renderer (for example Markwon via `AndroidView`, or a
  pure-Compose FOSS library).
- A YAML parser for the frontmatter (for example SnakeYAML Engine or kaml).
- Kotlin coroutines and a ViewModel per screen.
- Store the token with Android Keystore-backed encryption, never in plain prefs.
- Store the repo list in DataStore.

## Architecture

Keep these layers separate so Phase 2 (editing via commit and push) only extends
the data layer.

1. `RepoStore`: clone, refresh, delete, and expose the local working directory
   per repo.
2. `BeanParser`: file to `Bean` data class. This is the only code that knows the
   bean format.
3. `BeanIndex`: in-memory list with filter and search queries, rebuilt on refresh.
4. UI: talks only to ViewModels, which talk to the index and store.

## F-Droid readiness (hard requirements)

- Only FOSS dependencies. No Google Play Services, Firebase, Crashlytics,
  analytics, or ads.
- No prebuilt binaries or jars committed to the repo.
- Include `LICENSE` (Apache-2.0).
- Fastlane metadata in `fastlane/metadata/android/en-US/`: `title.txt`,
  `short_description.txt`, `full_description.txt`, and `changelogs/1.txt`.
  Leave an `images/phoneScreenshots/` folder with a `.gitkeep`.
- The README and full description must state that this is an **unofficial**
  companion for hmans/beans.

## Quality bar and definition of done

- `./gradlew assembleDebug` succeeds.
- `./gradlew test` passes, with unit tests for `BeanParser` built on fixture files
  copied from the real hmans/beans `.beans/` directory, plus edge cases (missing
  optional fields, empty body, malformed frontmatter that is skipped, not
  crashing).
- Instrumented end-to-end tests cover the flows listed in milestone 04.
- `./gradlew lint` has no errors.
- Coverage is at least 70 percent overall and at least 80 percent on the parser
  and index packages.
- The app handles at least a few hundred beans without noticeable lag.
- The README covers what the app is, how to build it, how to add a repo and
  token, and the Phase 2 roadmap (editing via commit and push).

## If you get stuck

- If JGit causes runtime issues on Android, document what you tried in the README
  and choose the most robust working configuration. Do not silently swap to a
  non-FOSS library.
- If the bean format has fields you are unsure about, parse them as generic
  key-value pairs and show them raw in the detail view, rather than dropping them.
