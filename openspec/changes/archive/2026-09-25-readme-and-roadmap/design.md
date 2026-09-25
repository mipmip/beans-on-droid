## The JGit constraint belongs in the README

It is already written up in the `repo-store` change's `design.md`, which is the
right permanent home for the reasoning. It is the wrong place to *find* it: an
archived change is something you read when you go looking, and someone running a
dependency update is not going looking.

So the README carries the short version with the evidence: the exact
`NoSuchMethodError`, why desugaring cannot fix it, why going back to 5.13 does
not work either, the table of which 6.x releases call `readNBytes`, and the one
instruction that matters, which is to run the instrumented tests on an API 26
emulator after any bump. The unit tests run on a desktop JVM and will pass
regardless, which is exactly the trap.

## Written for two readers

Someone installing the app needs three things: what it is, that it is unofficial,
and how to add a repository with a token. Those come first.

Someone changing the code needs the layering, the format document, and the JGit
warning. Those come after, rather than being spread through the first half where
they would get in the way.

## The roadmap says what will not happen

"Not planned: any use of the GitHub API, background sync, notifications, or
telemetry" is as much a part of the roadmap as the features. Those are the
briefing's non-goals, and stating them stops each one being proposed as an
obvious improvement.
