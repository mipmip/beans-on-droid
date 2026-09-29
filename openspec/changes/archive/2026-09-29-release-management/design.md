## Context

Two beans, one decision underneath them. The version and changelog work is
mechanical. The choice of who signs the app is not, and it is the part that
cannot be undone.

## The signing decision

F-Droid builds from source on its own infrastructure and signs with its own
key. You never send them an APK. An interim APK published here is signed with
ours. Android identifies an app by package name and signer together, so:

```
   friend installs      io.github.mipmip.beansondroid
   our GitHub APK       signed by OUR key
          │
          │  months later, F-Droid lists the app
          ▼
   F-Droid build        io.github.mipmip.beansondroid
                        signed by F-DROID'S key
          │
          ▼
   upgrade REFUSED.  Uninstall first, losing every configured
                     repository and every stored token.
```

That is the cost of shipping before inclusion, and it lands on exactly the
people who helped early.

There is one way out. F-Droid's reproducible build path: they build from
source, compare their output byte for byte against the APK we published, and
if it matches they distribute **our** signature instead of theirs. Continuity
holds and nobody uninstalls.

Most Android projects cannot reach that because their toolchain is not pinned.
This one already pins JDK 17, the Android SDK, build-tools 37.0.0 and AGP
through the flake, which is the difficult half. What remains is the usual
sources of nondeterminism: timestamps embedded by Gradle, zip entry ordering,
and R8. Minification is off, which removes one of the three.

So the position this change takes:

- Create the key now, because friends need an APK now.
- Treat the key as permanent from the first release, not as a placeholder.
- Aim for reproducible builds when submitting to F-Droid, and keep this build
  as deterministic as is free in the meantime.
- Say plainly in the README and in the first release notes that an interim
  install may need one uninstall if reproducibility does not land, so nobody
  discovers it as a surprise.

The F-Droid submission itself, the metadata recipe and the reproducibility
verification, is separate work and belongs in its own bean.

### The key is an operational liability

A keystore that only exists on one laptop is one disk failure away from an app
that can never be updated again. `docs/RELEASING.md` states the backup
obligation as part of the release procedure rather than as advice, because the
failure is silent until the day it matters.

The key never enters the repository. The signing configuration reads a local
`keystore.properties` first and the environment second, and when neither is
present it produces an unsigned build rather than failing, so a contributor can
still build the release variant.

### The file comes first because the daemon caches the environment

The first version read only environment variables. It looked right and it was
wrong: the Gradle daemon keeps the environment it started with, so variables
exported for a later invocation are invisible to it. The build then succeeded
and produced `app-release-unsigned.apk` without a word.

That is the worst possible failure for this feature. A release script that
quietly ships an unsigned artefact is worse than one that crashes.

Two changes came out of it:

- **A file is read on every build**, so it cannot go stale. `keystore.properties`
  at the root, mode 600, gitignored, is the conventional Android answer and it
  keeps the password off the command line where `ps` could see it.
- **Half a configuration is an error.** If a keystore path is given but the file
  is missing, or a keystore is found but the password or alias is not, the build
  fails and says which piece is absent. Only a complete absence of signing
  configuration is allowed to fall through to an unsigned build.

Verified in all three states: nothing configured builds unsigned and succeeds,
a bad path fails naming the path, a keystore without a password fails naming
the missing fields.

## Version

### One file, because two consumers

`VERSION` at the top level holding `0.2.0` and nothing else. Gradle reads it;
`release.sh` reads and writes it. Both are one line in their own language.

`gradle/libs.versions.toml` was the alternative and matches how `compileSdk`,
`minSdk` and `buildTools` are already handled. It loses because the release
script is shell, and `cat VERSION` beats grepping TOML for a value that a
version bump then has to rewrite in place.

### versionCode is derived, with room to rebuild

F-Droid orders releases by `versionCode` and it can never decrease. Maintaining
it by hand alongside a semantic version is two numbers that drift silently.

```
   versionCode = major*1000000 + minor*10000 + patch*100 + rebuild

   0.2.0  →     20000
   0.2.1  →     20100
   1.0.0  →   1000000
```

The `rebuild` slot exists for the case a derived scheme otherwise cannot
survive: the same version has to be published twice because the first artefact
was wrong. It is zero unless deliberately overridden. Without it the only
escape is an untrue patch bump.

The ceiling is 2100000000, so major versions up to 2100 fit.

`versionCode 1` has already been used by the debug build on a phone. 20000
clears it.

### The release touches four places

```
   VERSION                                      0.2.0
   app/build.gradle.kts                         reads it, derives 20000
   CHANGELOG.md                                 [Unreleased] → [0.2.0] - date
   fastlane/…/changelogs/20000.txt              written from that section
```

The fourth is the one a generic release script forgets. It is what F-Droid
shows as "What's New", it is named after the `versionCode` rather than the
version name, and it renders in a small card, so the script writes the section
as plain text with the markdown heading markers stripped.

## Two commands, two meanings

```
   ship-change.sh <change>     one OpenSpec change landed
                               gate → archive → commit → push

   release.sh patch|minor|major   one thing that exists for people
                               bump → roll changelog → write fastlane file
                               → commit → tag → push
                                      └── the tag is what CI reacts to
```

Neither implies the other. Landing work is not releasing it, and a release can
gather many landed changes. `--dry-run` prints every file it would touch and
the tag it would create, because a release script that cannot be rehearsed gets
rehearsed in production.

## CI

### Through nix, despite the cost

The runner could use its preinstalled Android SDK and be quicker. It would also
mean CI validates a toolchain nobody builds with, which removes the reason the
flake exists. CI installs nix and runs `./scripts/gate.sh`, the same entry point
used locally.

The first run pulls an SDK closure of a few gigabytes. A nix cache action keeps
later runs tolerable. This is a known, accepted cost.

### Three workflows, by how long they take

| Workflow | Trigger | Runtime |
|---|---|---|
| `gate` | push, pull request | minutes |
| `release` | tag `v*` | minutes |
| `e2e` | manual | slower, boots an emulator |

The instrumented suite stays off the per-push path for the same reason it is
outside `gate.sh`: an emulator boot is minutes and a failure there is more often
the emulator than the app. GitHub's Linux runners do expose KVM and the AVD is
already an API 26 AOSP x86_64 image, so the action works; it is the latency that
keeps it on request.

## What the signer change means, demonstrated

On an emulator, with the release key in place:

```
adb install app-release.apk          Success   (versionCode 20000, 0.2.0)
adb install -r app-debug.apk         Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE:
                                     signatures do not match the previously
                                     installed version]
```

That is the same refusal a user will meet moving between an interim build and
an F-Droid one, reproduced deliberately rather than described.

Release signer: `CN=Beans on Droid, O=mipmip, C=NL`.
Debug signer: `C=US, O=Android, CN=Android Debug`.

## Risks

- **The interim channel strands its users if reproducibility fails.** The
  mitigation is disclosure from the first release rather than a promise, and
  the reproducible attempt being part of the F-Droid submission rather than an
  afterthought.
- **A workflow cannot be verified by the gate.** `release.sh` can be
  shellchecked and dry-run locally, and it is. The workflows can only be proven
  by running, which means the first tag is also the first test of the release
  path. Cutting `0.2.0` deliberately, with the artefact checked before it is
  announced, is the way to absorb that.
- **The changelog currently holds the entire build under `[Unreleased]`.**
  Rolling it wholesale into `0.2.0` produces a long first entry. That is
  accurate, and the fastlane card is where it gets shortened.
