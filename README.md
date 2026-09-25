# Beans on Droid

An **unofficial** Android app for browsing and reading issues ("beans") from the
flat-file issue tracker [hmans/beans](https://github.com/hmans/beans). Not
affiliated with or endorsed by the beans project.

Beans keeps its issues as Markdown files with YAML frontmatter in a `.beans/`
directory inside your git repo. Beans on Droid clones such a repo read-only over
HTTPS and lets you filter, search, and read those issues on a phone.

Phase 1, the current scope, is read-only. Phase 2 adds editing via commit and
push.

## Status

Early. The project is scaffolded and the work is planned as milestones and epics
in `.beans/`; the app itself is still being built. See
[docs/BRIEFING.md](docs/BRIEFING.md) for the full Phase 1 specification.

## Building

With nix:

```bash
nix develop
./gradlew assembleDebug
```

Without nix you need JDK 17, the Android SDK with platform 36 and build-tools
36.0.0, and `ANDROID_HOME` pointing at the SDK. Then `./gradlew assembleDebug`.

Run the full quality gate with `./scripts/gate.sh`.

## Planned usage

Add a repo by its HTTPS clone URL. For a private repo, supply a personal access
token; it is stored encrypted with an Android Keystore-backed key, never in plain
preferences. The app shallow-clones the repo into app-private storage and indexes
every bean it finds. Pull to refresh fetches and hard-resets to the remote branch.

## Roadmap

- **Phase 1 (current):** read-only browsing, filtering, search, and detail view.
- **Phase 2:** editing beans, committing, and pushing back to the remote.

## License

Apache-2.0. See [LICENSE](LICENSE).
