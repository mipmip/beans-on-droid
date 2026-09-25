## 1. Dependency audit

- [x] 1.1 List every group on the release runtime classpath and its licence
- [x] 1.2 Verify no Play Services, Firebase, Crashlytics, analytics or ads
- [x] 1.3 Explain the `listenablefuture` placeholder artifact
- [x] 1.4 Confirm the app declares only the internet permission

## 2. Binaries

- [x] 2.1 Confirm the wrapper jar is the only committed binary
- [x] 2.2 Pin `distributionSha256Sum` in `gradle-wrapper.properties`
- [x] 2.3 Record the wrapper jar's checksum and state the deviation

## 3. Metadata

- [x] 3.1 Confirm title and short description are within their length limits
- [x] 3.2 Confirm the full description states the app is unofficial
- [x] 3.3 Confirm the changelog matches `versionCode = 1`

## 4. Screenshots

- [x] 4.1 `ScreenshotTest` driving the app into four states worth showing
- [x] 4.2 Capture via `screencap` as the shell user, so no storage permission is
      added
- [x] 4.3 `scripts/screenshots.sh` capturing and pulling into the metadata
- [x] 4.4 Four screenshots committed

## 5. Record

- [x] 5.1 `docs/fdroid.md` with the audit and how to reproduce it
