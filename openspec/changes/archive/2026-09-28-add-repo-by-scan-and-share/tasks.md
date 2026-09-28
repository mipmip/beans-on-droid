## 1. URL capture, pure

- [x] 1.1 Extract the first `http` or `https` address from arbitrary text
- [x] 1.2 Strip trailing punctuation from an extracted address
- [x] 1.3 Report that no address was found rather than returning an empty string
- [x] 1.4 Normalise: drop the fragment and the query string
- [x] 1.5 Normalise: truncate at a `/-/` segment
- [x] 1.6 Normalise: truncate at a known view segment, from the third path
      segment onward only
- [x] 1.7 Normalise: drop a trailing slash, leave `.git` untouched
- [x] 1.8 Return an unrecognised host's URL unchanged
- [x] 1.9 Table-driven tests covering every row measured in the design, plus the
      repository named `issues`
- [x] 1.10 Never read credentials from a captured URL

## 2. Dependencies and manifest

- [x] 2.1 Add a stable CameraX and `com.google.zxing:core` to the version
      catalog and the app module
- [x] 2.2 Declare `CAMERA`, and `uses-feature` camera with `required="false"`
- [x] 2.3 Add an `ACTION_SEND` / `text/plain` intent filter, and nothing else

## 3. Scanner

- [x] 3.1 Scanner screen with a camera preview bound to the lifecycle
- [x] 3.2 Image analyser turning a frame's luminance plane into a decode attempt
- [x] 3.3 On a decoded address, close and return the normalised URL
- [x] 3.4 On a decoded non-address, say so and keep scanning
- [x] 3.5 Release the camera when the scanner leaves the screen
- [x] 3.6 Unit test the decoder against generated QR bitmaps, off device
- [x] 3.7 Pack camera frames to width, and unit test the stride handling

## 4. Permission

- [x] 4.1 Request camera access when scanning is first chosen
- [x] 4.2 Explain why when access is refused, keeping typing and pasting usable
- [x] 4.3 Point at system settings when access is refused permanently
- [x] 4.4 Hide the scan action on a device with no camera

## 5. Share and paste

- [x] 5.1 Handle a share on a cold start
- [x] 5.2 Handle a share while the app is running, through `onNewIntent`
- [x] 5.3 Open the add form with the captured URL in place
- [x] 5.4 Say so when shared text holds no address, rather than opening an empty
      form
- [x] 5.5 Paste action on the URL field, running the same pipeline

## 6. Form

- [x] 6.1 Scan and paste actions on the URL field
- [x] 6.2 Accept an externally captured URL as the form's opening state
- [x] 6.3 Nothing is cloned or added until the person confirms
- [x] 6.4 Content descriptions on the new actions

## 7. Documentation

- [x] 7.1 Update `docs/fdroid.md`: the app now declares two permissions, with
      the reason and the scope of the second
- [x] 7.2 Update the README's section on adding a repository
- [x] 7.3 Note the new dependencies and their licences in the F-Droid audit

## 8. Verification

- [x] 8.1 Instrumented: sharing a URL opens the form filled in
- [x] 8.2 Instrumented: sharing a page URL fills in the clone URL
- [x] 8.3 Instrumented: sharing text with no URL is reported
- [x] 8.4 Instrumented: refusing the camera leaves the form usable
- [x] 8.5 Instrumented: a captured URL clones only after confirmation
- [x] 8.6 `./scripts/gate.sh` and `./scripts/e2e.sh` pass
