## Why

Bean `beans-on-droid-vcs8`. Typing a repository URL on a phone keyboard is the
worst moment in the app, and it is the very first thing anyone does. The bean
asks for QR scanning; sharing a URL from a browser or the GitHub app solves the
same problem from a different direction and costs almost nothing once the
scanning work is done.

There is a trap underneath both. A QR code generated from a browser, and a share
from the GitHub app, both carry the **page** URL, not the clone URL. Measured
against GitHub:

```
https://github.com/hmans/beans.git                 clones
https://github.com/hmans/beans                     clones
https://github.com/hmans/beans?tab=readme-ov-file  fails
https://github.com/hmans/beans/issues              fails
https://github.com/hmans/beans/tree/main/pkg       fails
```

GitHub routinely appends `?tab=readme-ov-file` to a repository page. So the
obvious implementation of this bean produces a scanner that reads a code
correctly and then fails to clone, which is worse than typing.

## What Changes

- Add an in-app QR scanner, reached from the add form, using CameraX and the
  ZXing decoder. This introduces the `CAMERA` permission, requested when the
  scan is first used.
- Accept a shared URL: register as a share target for plain text and extract the
  first web address from whatever was shared.
- Add a paste action to the URL field.
- Add a normaliser that turns a forge page URL into a clone URL by dropping the
  query and fragment and truncating at a view path.
- Route all three transports through one pipeline that ends at the existing add
  form, prefilled and editable, with nothing cloned until the user confirms.

## Capabilities

### New Capabilities

- `repo-url-capture`: getting a repository URL into the app without typing it,
  covering extraction from arbitrary text, normalisation of forge page URLs, and
  the rule that a captured URL is never acted on by itself.

### Modified Capabilities

- `repo-switcher`: the add form gains a scan action and a paste action, and can
  be opened already filled in from outside the app.

## Impact

- New: a scanner screen, a URL capture package, an intent filter for
  `ACTION_SEND` with `text/plain`.
- New permission: `CAMERA`, with `uses-feature` marked not required so the app
  still installs on a device without one.
- New dependencies, both Apache-2.0: CameraX (`camera-core`, `camera-camera2`,
  `camera-lifecycle`, `camera-view`) and `com.google.zxing:core`.
- `docs/fdroid.md` claims the app declares one permission. That claim changes
  and the document has to change with it.
- The normaliser is pure and belongs under the 80 percent rule.
