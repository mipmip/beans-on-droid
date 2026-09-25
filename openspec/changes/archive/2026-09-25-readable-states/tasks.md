## 1. Stale refresh

- [x] 1.1 `IndexState.Ready` carries a nullable stale reason
- [x] 1.2 A failed refresh keeps `Ready` and sets the reason when that
      repository's beans are showing
- [x] 1.3 A failed refresh with nothing showing still fails
- [x] 1.4 Banner on the list with the reason and a retry

## 2. Recreation

- [x] 2.1 `loadActiveIfNeeded` skipping when the active repository is already
      showing
- [x] 2.2 View model init uses it

## 3. Unreadable files

- [x] 3.1 The count is tappable
- [x] 3.2 A dialog listing each file and the parser's reason

## 4. Detail view

- [x] 4.1 Distinguish index-not-ready from bean-not-found, with the reason

## 5. Tests

- [x] 5.1 Unit: failed refresh keeps beans and sets a reason
- [x] 5.2 Unit: a later successful refresh clears it
- [x] 5.3 Unit: failure with nothing showing still fails
- [x] 5.4 Unit: `loadActiveIfNeeded` preserves a stale marker and loads when idle
- [x] 5.5 Instrumented: no repository, no beans, missing bean directory
- [x] 5.6 Instrumented: unreachable repository with a retry
- [x] 5.7 Instrumented: unreadable files counted and listed
- [x] 5.8 Instrumented: failed refresh keeps beans and offers retry
- [x] 5.9 Instrumented: detail view does not blame a missing bean
- [x] 5.10 Fix the empty-bean-directory fixture so git preserves it
