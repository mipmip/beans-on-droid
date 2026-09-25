## Why

Bean `beans-on-droid-coop`. The screens handle their happy paths and each has
some empty state, but reviewing them against the briefing turned up three places
where the app says something untrue: a failed refresh throws away beans it
already has, the detail screen claims a bean is missing when the index simply is
not loaded yet, and the count of unreadable files names none of them.

## What Changes

- A failed refresh keeps the beans already on screen and shows a banner saying
  the copy is stale, with the reason and a retry, instead of replacing the list
  with an error.
- Creating a view model no longer reindexes a repository that is already
  showing, so a rotation does not silently clear that banner.
- The count of files that could not be read opens a list of them with the reason
  for each.
- The detail screen distinguishes a bean that is not in the repository from an
  index that is not ready.

## Capabilities

### New Capabilities

- `readable-states`: what the app says when it has nothing, is busy, or has
  failed.

### Modified Capabilities

None. The screens' own specs describe their happy paths; this capability covers
the states across them.

## Impact

- Modified: `data/BeansRepository.kt`, `viewmodel/AppViewModel.kt`,
  `ui/screen/BeanListScreen.kt`, `ui/screen/BeanDetailScreen.kt`.
- No new dependencies.
