## Why

Bean `beans-on-droid-b3df`. The data layer is complete and tested but there is
no app around it, only a placeholder screen. This change adds the shell: the
object that turns a repository into an index, the state every screen reads, and
the navigation between the three screens. The screens themselves stay stubs and
are filled in by the epics that follow, so the plumbing can be tested before
there is any layout to hide behind.

## What Changes

- Add `BeanLoader`: a bean directory in, an index plus the list of skipped files
  out.
- Add `BeansRepository`: the single coordinator over the registry, the store and
  the loader, exposing one observable `IndexState`.
- Add `AppViewModel`: the state every screen reads, with the query, the add-repo
  form and the refresh flag, all driven off injectable dispatchers.
- Add `RepoUrl.validate`, and apply it in the form rather than in the data layer.
- Add Navigation Compose with three routes, and wire the activity to a
  `BeansRepository` created once in the application.
- Extract `RepoCatalog` as the registry's interface, so the coordinator can be
  unit tested without a device.

## Capabilities

### New Capabilities

- `app-shell`: the state the whole app moves through, from having no repository
  to showing beans, and what the user can navigate between.

### Modified Capabilities

None.

## Impact

- New: `data/`, `viewmodel/`, `ui/Navigation.kt`, three stub screens,
  `store/RepoUrl.kt`.
- Modified: `MainActivity`, `BeansOnDroidApplication`, `RepoRegistry` now
  implements `RepoCatalog`.
- No new dependencies.
