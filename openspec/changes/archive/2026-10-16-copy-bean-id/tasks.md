## 1. The copyable id

- [x] 1.1 Turn the id in the detail header into a chip carrying a copy icon, keeping the id itself visible and the header layout (title, id, pills) intact; verify with the existing detail screen tests still passing and the id still shown
- [x] 1.2 Copy the bean's bare id to the system clipboard when the chip is chosen, through the platform `ClipboardManager` the repository screen already resolves; verify with an instrumented test that reads the clipboard back and asserts it equals the id
- [x] 1.3 Give the copy affordance a content description naming the action; verify the affordance is reachable by `onNodeWithContentDescription` in the instrumented test

## 2. Confirmation

- [x] 2.1 Add a `SnackbarHost` to the detail `Scaffold`; verify the detail screen still renders its sections with the host in place
- [x] 2.2 Show a confirmation naming the copied id when the chip is chosen; verify with an instrumented test asserting the message appears after the tap
- [x] 2.3 Have the confirmation clear itself without a further tap; verify the snackbar is shown with a short duration and no action

## 3. The missing-bean path

- [x] 3.1 Verify no copy affordance is offered when the detail view is opened for an id the repository does not have, with an instrumented test alongside `anUnknownBeanDegradesGracefully`

## 4. Verification

- [x] 4.1 `./gradlew test` passes
- [x] 4.2 `./gradlew lint` passes
- [x] 4.3 `./gradlew connectedAndroidTest` passes
- [x] 4.4 Coverage stays at or above the floor enforced by the gate
- [x] 4.5 `./scripts/gate.sh` passes
- [x] 4.6 Confirmed on an emulator below API 33 that the in-app confirmation is what tells you the copy happened
