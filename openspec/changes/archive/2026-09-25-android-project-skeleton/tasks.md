## 1. Gradle project

- [x] 1.1 `settings.gradle.kts` with repositories and the `:app` module
- [x] 1.2 Root `build.gradle.kts` declaring the plugins without applying them
- [x] 1.3 `gradle/libs.versions.toml` holding every version and library alias
- [x] 1.4 Gradle wrapper pinned to 9.8.0, with `gradlew` executable

## 2. App module

- [x] 2.1 `app/build.gradle.kts`: applicationId `io.github.mipmip.beansondroid`,
      minSdk 26, compileSdk and targetSdk 36, versionCode 1, versionName 0.1.0
- [x] 2.2 Enable Compose, Java 17 source/target and core library desugaring
- [x] 2.3 `AndroidManifest.xml` with a single exported launcher activity and the
      INTERNET permission
- [x] 2.4 Material 3 theme that follows the system light and dark setting
- [x] 2.5 Single activity hosting a placeholder composable

## 3. Verification

- [x] 3.1 `nix develop -c ./gradlew assembleDebug` produces a debug APK
- [x] 3.2 `nix develop -c ./gradlew lint` reports no errors
- [x] 3.3 No version literal appears outside `gradle/libs.versions.toml`
- [x] 3.4 The APK's manifest shows minSdkVersion 26 and the fixed identity

## 4. Gate

- [x] 4.1 Point `scripts/gate.sh` at tasks that exist today (`assembleDebug`,
      `test`, `lint`), leaving the coverage step to `beans-on-droid-f006`
