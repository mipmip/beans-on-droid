## Why a real HTTP server, and why smart protocol

The existing tests clone from `file://`. That exercises JGit's object handling
but not its transport, not authentication, and not the shallow negotiation the
app relies on. Since the briefing's whole data layer is "plain git over HTTPS",
leaving HTTP untested would leave the most fragile part unverified.

The first attempt served the **dumb** HTTP protocol: static files plus a
generated `info/refs`. It does not work, and the reason is instructive.

```
clone failed: Unknown: Invalid remote: origin
```

Shallow clone is a negotiation. `setDepth(1)` requires the client and server to
agree on what is being omitted, and the dumb protocol has no negotiation at all:
it is a file listing. So a dumb server cannot serve this app, because this app
always clones shallow.

The **smart** protocol is served by JGit itself. `UploadPack` with
`setBiDirectionalPipe(false)` is exactly what a smart HTTP endpoint needs, so the
server is two endpoints over roughly a hundred lines:

- `GET /info/refs?service=git-upload-pack`, answered with a pkt-line service
  header followed by `UploadPack.sendAdvertisedRefs`.
- `POST /git-upload-pack`, answered by feeding the request body to
  `UploadPack.upload`.

Two details cost a debugging round each, and both are worth writing down because
any hand-rolled git HTTP server hits them:

1. **JGit gzips the request body.** It sends `Content-Encoding: gzip` on the
   POST. Without decompressing, `UploadPack` throws
   `UploadPackInternalServerErrorException` with a null message, which tells you
   nothing. The server now gunzips when that header is present.
2. **The body may be chunked.** `Content-Length` is not guaranteed, so the server
   handles `Transfer-Encoding: chunked` as well.

Getting there needed the server to log its requests. That logging stayed, because
the next person to touch this will need it.

## Authentication is real, not simulated

The server takes an optional token and answers 401 with a `WWW-Authenticate`
header when the HTTP basic username does not match. So the bad-token test
exercises JGit's credential handling, the app's error classifier, and the form's
error display, rather than a mocked failure. The matching good-token test proves
the 401 is about the token and not about the server being broken.

## A test that asserted nothing

The first version of the refresh test committed a new bean, navigated away and
back, and asserted the list showed "3 of 3". That is the count from *before* the
commit. It passed, and it would have passed just as happily if refresh were
deleted entirely.

It now performs an actual pull gesture on the list and asserts "4 of 4" and the
new bean by name. Making it assert the right thing made it fail, which is how the
next problem surfaced: a swipe on a small text node is too short to cross the
refresh threshold, so the list carries a test tag and the gesture spans it.

## What runs where

`scripts/e2e.sh` boots a headless AOSP emulator, runs the instrumented tests and
shuts it down, so CI needs one command. The gate deliberately does not run it:
the emulator takes minutes to boot and a ship should not depend on it. The
instrumented suite is the pre-release check, the unit gate is the per-change one.
