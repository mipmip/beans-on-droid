## Context

Adding a repository is the first thing anyone does and the worst moment in the
app: a long URL on a phone keyboard. The bean asks for QR scanning. Sharing from
a browser or the GitHub app reaches the same place, and once the scanning
pipeline exists it is nearly free.

## The problem underneath the bean

A scanner that reads codes correctly is not enough, because of what the codes
contain. Checked against GitHub with `git ls-remote`:

| URL | Result |
|---|---|
| `https://github.com/hmans/beans.git` | clones |
| `https://github.com/hmans/beans` | clones |
| `https://github.com/hmans/beans?tab=readme-ov-file` | fails |
| `https://github.com/hmans/beans/issues` | fails |
| `https://github.com/hmans/beans/tree/main/pkg` | fails |

Every realistic producer of these codes emits a **page** URL. A browser's "QR
code for this page" gives whatever is in the address bar, and GitHub routinely
puts `?tab=readme-ov-file` there. The GitHub app's share sheet has the same
property, and if you were looking at a file or an issue when you generated the
code, the path is not a repository at all.

Shipping the scanner without the normaliser would produce a feature that reads
the code perfectly and then fails to clone, which is worse than typing, because
the failure arrives thirty seconds later and blames the network.

## Decisions

### Three transports, one pipeline

```
   scan ───┐
           ├──▶ extract URL ──▶ normalise ──▶ add form, filled and editable
   share ──┤                                  ──▶ person confirms ──▶ clone
   paste ──┘
```

Extraction and normalisation are pure functions over strings with no Android in
them, which is what makes the table above expressible as unit tests rather than
as a paragraph. The transports differ only in where the string comes from.

### Normalise, then show your work

The normaliser is a heuristic and will sometimes be wrong. The mitigation is not
a cleverer heuristic, it is that the result always lands in a visible, editable
field and nothing happens until the person presses Add. A wrong guess costs one
correction; a silent wrong clone costs a confusing error later.

The rules, in order:

1. Drop the fragment and the query string. This alone fixes the most common case.
2. Truncate at a `/-/` segment. GitLab puts that between the project path and
   the view, which survives nested subgroups where counting segments would not.
3. Truncate at a known view segment, considered only from the third path segment
   onward: `tree`, `blob`, `raw`, `src`, `commit`, `commits`, `issues`, `pull`,
   `pulls`, `merge_requests`, `releases`, `tags`, `wiki`, `actions`, `compare`,
   `branches`, `settings`.
4. Drop a trailing slash.

The depth guard in rule 3 is not a detail. A repository can be called `issues`,
and `github.com/someone/issues` must survive, so a marker in the second segment
is part of the repository path and never a view.

`.git` is left exactly as found. GitHub, GitLab and Gitea all serve both forms,
so adding it would be churn, and removing it would break a host that wanted it.

These are forge conventions, not API calls, so this does not breach the
briefing's rule against using the GitHub API. An unrecognised host falls through
every rule and is returned unchanged, which is the honest outcome.

### The permission is the real cost

The app declares one permission, and `docs/fdroid.md` makes a point of it. This
change ends that, and the document has to say so rather than quietly dropping
the claim.

What keeps the cost proportionate:

- `CAMERA` is requested when scanning is first chosen, not at launch.
- `uses-feature android:name="android.hardware.camera" android:required="false"`,
  so the app still installs on a device without one, and the scan action is
  simply not offered there.
- The camera runs only while the scanner is on screen.
- Refusing access leaves everything else working. The scanner is a shortcut, not
  a gate, and the typed field never goes away.

A custom `beansondroid://` scheme was considered and rejected. It would give
scanning with no permission at all, because the phone's own camera app would
resolve it, but only for codes generated specifically for this app. The whole
premise of the bean is codes made by generic tools from ordinary URLs, and those
carry `https://`, which a system scanner hands to a browser. Registering as an
App Link for `https://` would require hosting `assetlinks.json` on github.com,
which we do not own.

### CameraX with the ZXing decoder, not a bundled scanner

ML Kit is Play Services and is out. Of the FOSS options:

| Option | Why not chosen |
|---|---|
| `com.journeyapps:zxing-android-embedded` | Batteries included, but wraps an older camera stack and owns its own activity and theming |
| `io.github.zxing-cpp:android` | Fastest, but ships prebuilt native libraries per ABI, which is more to audit for an F-Droid build |
| CameraX plus `com.google.zxing:core` | Chosen |

`com.google.zxing:core` is a pure Java decoder with no Android in it, so the
decode step is unit testable off a device. The glue is an image analyser turning
a camera frame's luminance plane into the decoder's input, which is small and
ours. CameraX is AndroidX, so it matches everything else in the project and
handles the lifecycle and rotation that make hand-rolled camera code miserable.

Pin a stable CameraX. The newest version on Maven is an alpha.

### Shares arrive at a running app too

The activity is a single instance hosting a navigation graph, so a share that
arrives while the app is open reaches `onNewIntent` rather than a fresh start.
Handling only the cold start would silently drop the share in what is probably
the common case, because you are likely to have had the app open recently.

Only `ACTION_SEND` with `text/plain` is claimed. Claiming `ACTION_VIEW` for
`https` would put this app in the chooser for every link tapped on the phone,
which would be hostile.

### No credentials from captured input

A URL can carry credentials, in `https://user:token@host/...` or as a query
parameter. None of it is read, and the token field is never populated from a
capture. A token in a QR code is a credential on a photographable surface and
in the intent history, and supporting it would invite exactly that. Typing the
token once is the right amount of friction for the thing that grants read access
to someone's source.

## Risks

- **The normaliser is a heuristic against conventions that can change.** A forge
  could introduce a path shape the rules mangle. The editable field is the
  backstop, and the rules are covered by table-driven tests that are cheap to
  extend when a real URL defeats them.
- **Scanner tests need a camera.** The decoder and both pure functions are unit
  testable, and the permission and form behaviour are testable on an emulator,
  but pointing a virtual camera at a generated QR code is awkward. The emulator
  supports a virtual scene; if that proves unreliable, the honest split is to
  test the decode path against generated bitmaps and the scanner screen's states
  without a live camera, and say so rather than claiming coverage that is not
  there.
- **APK size grows** by roughly two megabytes for CameraX and the decoder, on an
  app whose value is reading text files.
