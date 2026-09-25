# F-Droid readiness

The briefing sets hard requirements for inclusion in F-Droid. This is the audit
against them, with the one deviation stated plainly.

## Dependencies are FOSS

Every artifact on the release runtime classpath, by group:

| Group                    | License             |
|--------------------------|---------------------|
| `androidx.*`             | Apache-2.0          |
| `org.jetbrains`, `org.jetbrains.kotlin`, `org.jetbrains.kotlinx` | Apache-2.0 |
| `org.eclipse.jgit`       | EDL 1.0 (BSD-3)     |
| `io.noties.markwon`      | Apache-2.0          |
| `com.atlassian.commonmark` | BSD-2-Clause      |
| `org.yaml` (SnakeYAML)   | Apache-2.0          |
| `org.slf4j`              | MIT                 |
| `com.googlecode.javaewah`| Apache-2.0          |
| `com.squareup.okio`      | Apache-2.0          |
| `org.jspecify`           | Apache-2.0          |
| `com.google.guava:listenablefuture` | Apache-2.0 |

All OSI-approved and all compatible with distributing an Apache-2.0 app.

`com.google.guava:listenablefuture:1.0` is the empty placeholder artifact that
exists purely to resolve a version conflict; it contains no code.

Verified absent from the release classpath: Google Play Services, Firebase,
Crashlytics, any analytics SDK, and any ad SDK.

```
./gradlew :app:dependencies --configuration releaseRuntimeClasspath \
  | grep -icE "play-services|firebase|crashlytics|gms|analytics|admob"
0
```

## Permissions

One: `android.permission.INTERNET`. Nothing else is declared. The app reads
repositories over HTTP and writes only to its own private storage.

## No network callbacks the user did not ask for

The app contacts exactly the hosts whose repository URLs the user typed, and only
when the user adds a repository or refreshes one. There is no background work, no
telemetry, and no update check.

## Binaries in the repository

The briefing says no prebuilt binaries or jars. There is exactly one:

```
gradle/wrapper/gradle-wrapper.jar   sha256 7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172
```

**This is a deviation, and it is deliberate.** The Gradle wrapper jar is the
standard bootstrap for a Gradle build, it is upstream Gradle's own artifact, and
without it `./gradlew assembleDebug` (which the briefing itself names as the
definition of done) does not work on a fresh clone. F-Droid's build server
handles wrapper jars as a matter of course.

Two things reduce the trust this asks for:

- `gradle-wrapper.properties` pins `distributionSha256Sum`, so the Gradle
  distribution the wrapper downloads is verified rather than trusted.
- The wrapper jar's own checksum is recorded above, so a change to it is visible
  in review rather than buried in a binary diff.

No dependency jars, no `.so` files and no prebuilt AARs are committed. Everything
else is resolved from Maven Central and Google's Maven repository at build time.

## Metadata

`fastlane/metadata/android/en-US/`:

- `title.txt`, within the 50 character limit
- `short_description.txt`, within the 80 character limit
- `full_description.txt`, stating that the app is unofficial
- `changelogs/1.txt`, matching `versionCode = 1`
- `images/phoneScreenshots/`, four screenshots captured from a real run on an
  API 26 emulator by `scripts/screenshots.sh`

## Versioning and licence

- `versionCode = 1`, `versionName = "0.1.0"`
- `LICENSE` is Apache-2.0
- The README states that the app is unofficial and not affiliated with
  `hmans/beans`

## Reproducing this audit

```bash
nix develop --command ./gradlew :app:dependencies --configuration releaseRuntimeClasspath
git ls-files | grep -iE '\.(jar|so|aar|dex|apk)$'
sha256sum gradle/wrapper/gradle-wrapper.jar
```
