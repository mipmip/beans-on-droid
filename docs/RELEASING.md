# Releasing

Two commands, two meanings. `ship-change.sh` lands one unit of work.
`release.sh` declares that a set of landed work exists for people. Neither
implies the other.

## The version

`VERSION` at the repository root holds the **last released** version and
nothing else. `release.sh` bumps from it, so it trails the tags by one release
rather than naming the version being worked towards.
Gradle reads it for `versionName` and derives `versionCode` from it:

```
versionCode = major*1000000 + minor*10000 + patch*100 + rebuild

0.2.0  →     20000
0.2.1  →     20100
1.0.0  →   1000000
```

F-Droid orders releases by `versionCode` and it can never decrease, which is
why it is derived rather than maintained by hand.

The `rebuild` slot is zero unless `BEANS_VERSION_REBUILD` says otherwise. It
exists for the one case a derived scheme cannot otherwise survive: the same
version has to be published twice because the first artefact was wrong. Use it
rather than an untrue patch bump.

## Cutting a release

```bash
./scripts/release.sh minor --dry-run   # rehearse, changes nothing
./scripts/release.sh minor             # do it
```

Rehearse first. The script prints every file it would touch and the notes it
would publish.

It then bumps `VERSION`, rolls `## [Unreleased]` into a dated section, writes
the fastlane changelog, commits with jj, tags `v<version>` and pushes both. The
tag is what the release workflow reacts to.

It refuses to run on a dirty working copy, on an empty `[Unreleased]`, or when
the tag already exists.

### The four files a release touches

```
VERSION                                      the version
app/build.gradle.kts                         reads it, derives the code
CHANGELOG.md                                 [Unreleased] → [0.2.0] - date
fastlane/…/changelogs/<versionCode>.txt      what F-Droid shows as "What's New"
```

The fourth is the one a generic release script forgets. It is named after the
`versionCode`, not the version name, and it renders in a small card, so keep it
short.

## The signing key

**Back up the keystore and its passwords before you use it once.** If you lose
them you can never update the app for anyone who installed a build signed with
them. There is no recovery, no reset, and no support channel. This is the most
fragile thing in the project.

Create it once:

```bash
nix develop -c keytool -genkeypair -v \
  -keystore ~/beans-on-droid-release.jks \
  -alias beansondroid -keyalg RSA -keysize 4096 -validity 10000
```

Then give it to GitHub Actions:

```bash
base64 -w0 ~/beans-on-droid-release.jks | gh secret set KEYSTORE_BASE64
gh secret set KEYSTORE_PASSWORD
gh secret set KEY_ALIAS
gh secret set KEY_PASSWORD
```

The keystore never enters the repository; `*.jks` and `*.keystore` are ignored.

To build a signed APK locally:

```bash
BEANS_KEYSTORE=~/beans-on-droid-release.jks \
BEANS_KEYSTORE_PASSWORD=… BEANS_KEY_ALIAS=beansondroid BEANS_KEY_PASSWORD=… \
  nix develop -c ./gradlew assembleRelease
```

Without those variables the release build is unsigned, so a contributor with no
key can still build it.

## What an interim release costs your users

F-Droid builds from source on its own machines and signs with **its** key. An
APK published here is signed with **ours**. Android identifies an app by package
name and signer together, so someone who installs a GitHub release cannot later
upgrade to the F-Droid build: they have to uninstall first, losing every
configured repository and every stored token.

One thing avoids that. F-Droid's reproducible build path: they build from
source, compare their output against the published APK byte for byte, and if it
matches they distribute our signature instead of theirs. Continuity holds and
nobody uninstalls.

This project is better placed for that than most, because the flake already
pins JDK 17, the Android SDK, build-tools and AGP, which is where reproducible
builds usually fail. What remains is the usual nondeterminism: timestamps
embedded by Gradle, zip entry ordering, and R8. Minification is off, which
removes one of the three.

Until that lands, say so in the release notes. Someone who installs early
should not discover the uninstall as a surprise.

## Workflows

| Workflow | Trigger | What it does |
|---|---|---|
| `gate` | push to main, pull request | `./scripts/gate.sh` through nix |
| `release` | tag `v*` | gate, build signed, publish with checksums |
| `e2e` | on request | boots an API 26 emulator, runs the instrumented suite |

`release` refuses a tag that disagrees with `VERSION`, and refuses to publish
at all when no signing key is configured, rather than quietly shipping an
unsigned APK.

`e2e` is on request because an emulator boot costs minutes and a failure there
is more often the emulator than the app.

## Checklist for the first release

1. Create the keystore and back it up.
2. Set the four repository secrets.
3. `./scripts/release.sh minor --dry-run` and read the output.
4. `./scripts/release.sh minor`.
5. Watch the release workflow. The first tag is also the first test of it.
6. Download the published APK, install it on a device, and only then tell
   anyone it exists.
