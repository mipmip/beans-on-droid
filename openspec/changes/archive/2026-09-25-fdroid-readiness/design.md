## The audit found nothing wrong, which is the point

Every group on the release classpath is Apache-2.0, BSD or MIT. The only
surprise was `com.google.guava:listenablefuture:1.0`, which looks like a Google
SDK creeping in and is in fact the empty placeholder artifact Guava publishes to
resolve a version conflict. It contains no classes. Worth naming in the document
so the next person does not have the same moment.

## The one deviation, stated rather than reinterpreted

The briefing says no prebuilt binaries or jars in the repository.
`gradle/wrapper/gradle-wrapper.jar` is a jar.

It could be removed. `./gradlew` would then fail on a fresh clone, and
`./gradlew assembleDebug` is the briefing's own definition of done, so removing
it would break one requirement to satisfy another. Committing it is also what
every Android project does and what F-Droid's build server expects.

So it stays, and rather than quietly redefining "jar" to exclude it, the document
says it is a deviation and does two things to reduce what it asks the reader to
trust:

- `distributionSha256Sum` in `gradle-wrapper.properties`, so the Gradle
  distribution the wrapper fetches is verified rather than assumed. This is a
  genuine supply-chain improvement that the project should have had anyway.
- The wrapper jar's own SHA-256 recorded in `docs/fdroid.md`, so changing it
  shows up in a review as a changed line of text and not only as an opaque binary
  diff.

## Screenshots are captured, not mocked up

`ScreenshotTest` drives the real app through the states worth showing, against a
small repository it builds itself, and takes the pictures with `screencap`.

Two things about that were not obvious:

- The test APKs are uninstalled after `connectedAndroidTest`, taking the app's
  private and app-external directories with them. The first attempt wrote to
  `getExternalFilesDir` and the files were gone before the script could pull
  them. They now go to shared storage.
- Writing to shared storage from the app would need `WRITE_EXTERNAL_STORAGE` in
  the manifest, which is a permission this app has no business holding. Running
  `screencap` through `uiAutomation.executeShellCommand` writes as the shell
  user instead, so the release manifest keeps its single `INTERNET` permission.

The screenshots are regenerated with `./scripts/screenshots.sh` against a running
emulator, so they can be kept current rather than becoming stale marketing.
