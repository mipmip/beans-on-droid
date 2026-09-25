# Beans on Droid

Unofficial Android app for browsing and reading issues ("beans") from the
flat-file issue tracker [hmans/beans](https://github.com/hmans/beans). Beans
stores issues as Markdown files with YAML frontmatter in a `.beans/` directory
inside a git repo. This app clones such repos read-only over HTTPS, parses the
beans, and lets you filter, search, and read them on a phone.

Phase 1 is read-only. Phase 2 adds editing via commit and push. The full
specification is in [docs/BRIEFING.md](docs/BRIEFING.md), which is the source of
truth for scope and for the quality bar.

Kotlin, Jetpack Compose, Material 3, JGit, minSdk 26, Apache-2.0, built to be
F-Droid ready.

## Commands

```bash
nix develop                       # dev shell: JDK 17, Android SDK, Gradle
nix flake check                   # flake-level checks (shellcheck, nix fmt)

./scripts/gate.sh                 # the full quality gate (flake + gradle)
./scripts/ship-change.sh <change> # gate, archive, commit, push

nix develop -c ./gradlew assembleDebug
nix develop -c ./gradlew test
nix develop -c ./gradlew lint
nix develop -c ./gradlew connectedAndroidTest   # e2e, needs an emulator

beans list                        # milestones and epics
beans show <id>
openspec list                     # active changes
openspec validate <change> --strict
```

### Why the build is not inside `nix flake check`

Gradle resolves its dependencies from the network. A nix derivation builds in a
sandbox without network access, so an Android build cannot run as a flake check
without vendoring every dependency, which is a project of its own.

So the split is: `nix flake check` validates the flake and the shell scripts,
and `./scripts/gate.sh` runs that plus the real Gradle build, tests, lint, and
coverage inside `nix develop`. `ship-change.sh` calls `gate.sh`, so the gate is
what blocks a ship. Treat `gate.sh` as the gate, not `nix flake check`.

The Gradle half of the gate is skipped while `./gradlew` does not exist yet, so
the very first change (the flake itself) can ship. From the moment the Gradle
skeleton lands, the full gate applies.

## Nix

Plain nix flakes. No flake-utils. Supported systems are listed explicitly and
mapped with `nixpkgs.lib.genAttrs`. Keep it that way. `flake.lock` is committed.

## Version control

`jj`, with the git backend. Remote is `git@github.com:mipmip/beans-on-droid.git`.

Commit after every archival of an OpenSpec change, which is what
`ship-change.sh` does. Commits are authored by Pim Snel. Never add
`Co-authored-by`, `Generated with`, or any other attribution trailer.

## OpenSpec

Every epic becomes an OpenSpec change proposal before any code is written for
it. The loop per epic is:

1. Pick the next epic with `beans list --ready`.
2. Set it to `in-progress`.
3. Write the proposal under `openspec/changes/<change-name>/`, linking back to
   the bean.
4. Validate with `openspec validate <change-name> --strict`.
5. Implement, checking off tasks in `tasks.md` as they land.
6. Ship with `./scripts/ship-change.sh <change-name>`, which gates, archives,
   commits, and pushes.
7. Set the bean to `completed` and add the `openspec-link` to its frontmatter.

## Beans

When I refer to issues like beans-on-droid-rn3b checkout the task
in @.beans/beans-on-droid-rn3b-*.md

In this project we will use these tasks as epics for making openspec proposals.

WHEN you create a proposal at a link to this task in the proposal.md.
WHEN a bean is used to create an proposal change the status to "in-progress"
WHEN a proposal is archived add the link to the archived proposal in the frontmatter of this task like this:

```
openspec-link: openspec/changes/archive/....
```

You are allowed to update these statuses in the task frontmatter:

- in-progress
- todo
- draft
- completed
- scrapped

When making changes you are allowed to update the date/time in `updated_at` in the task frontmatter

Besides updating status and openspec-link, you are NOT ALLOWED to modify the contents of the task file.

### Milestones and epics

Milestone titles start with an incrementing two-digit number beginning `01`.
Epics hang under a milestone as children. Concrete work below an epic is tracked
in the OpenSpec change's `tasks.md`, not as more beans.

## Code style

No comments in code. The code says what it does. Comments are allowed only where
an OpenSpec document, a bean, or this file requires them.

## Autonomous build

The full run loop for building this project unsupervised is in
[docs/AUTONOMOUS-BUILD.md](docs/AUTONOMOUS-BUILD.md).
