## Context

The version lives in `VERSION`, is read by Gradle and derived into a
`versionCode`. None of that reaches the screen, and `BuildConfig` is not
generated at all, so there is nothing to read even if a screen wanted it.

## Decisions

### `BuildConfig`, not a hand-written constant

Enabling `buildFeatures { buildConfig = true }` makes AGP emit
`BuildConfig.VERSION_NAME` and `VERSION_CODE` from the values the build already
computed. A constant in Kotlin would be a fourth place the version is written
and a fourth place it can drift from `VERSION`.

The spec says the displayed value must be the running build's, not one written
separately by hand, which is what rules that alternative out.

### The repository screen, not an About dialog

The app has three screens and none of them is settings. The repository screen
is the closest thing: it is where you go to change how the app is configured,
and it is reachable from the bean list in one tap. An About dialog would be a
fourth screen carrying one line of text.

The footer sits outside the list and the empty state, so it shows in both. That
is deliberate: the moment someone most needs to report a version is when the
first clone failed and the list is empty.

### The version code is shown, not hidden

`0.2.0 (20000)` rather than `0.2.0`. The name is what a person says; the code is
what F-Droid orders by and what identifies the build unambiguously when a
version is republished. Both are cheap to show and the parenthesis is a
convention people already read.
