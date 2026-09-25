## Context

Four things below the UI needed a home: turning files into an index, deciding
what the app is currently doing, holding the user's query, and knowing which
screen is showing.

## Decisions

### `BeansRepository` owns the state machine, the view model owns the form

`IndexState` has four cases and one writer. Every transition happens in
`BeansRepository`, so there is no way for two components to disagree about
whether the app is loading. `AppViewModel` holds only what belongs to the
interface: the current query, the add-repo form, and whether a pull-to-refresh
spinner should be showing.

### `RepoCatalog` exists so the coordinator can be tested

`RepoRegistry` needs a DataStore, which needs Android. Extracting the interface
it already satisfies lets `BeansRepositoryTest` run on the JVM against a fake and
a real local git repository. That is the difference between the coordinator being
covered by eleven fast tests and being covered by nothing.

### URL validation moved out of the data layer

It was briefly in `BeansRepository`, which was wrong twice over: it is input
validation, and it made the data layer untestable against local repositories.
`RepoUrl.validate` is a pure function, unit tested on its own, and applied by the
view model before it calls anything.

The view model takes the validator as a constructor parameter defaulting to the
real one. Its tests for invalid input use the default; its tests for the success
path inject a permissive one so they can point at a local repository. Without
that seam the success path could only be tested against the network.

### Dispatchers are injected, not assumed

`AppViewModel` takes the IO dispatcher and `RepoStore` takes one too. The tests
pass `StandardTestDispatcher` and drive time with `advanceUntilIdle`, so the
asynchronous behavior (the refresh flag going up and coming back down, the form
unlocking after a failure) is asserted rather than slept on.

### Skipped files travel with the index

`LoadedBeans` carries both the index and the files that could not be parsed. The
parser was built so one bad file cannot take the repository down; carrying the
list to the UI is what makes that visible instead of merely survivable.

### Stub screens

The three screens land as stubs. The alternative, building the shell and the
first screen together, would mean the navigation and the state machine arrive
untested underneath a layout. Each following epic replaces one stub.

## Risks

- `BeansRepository` is created in the application object and held for the process
  lifetime, so the index stays in memory while the app is backgrounded. That is
  the point at a few hundred beans, and the performance epic is where it gets
  measured rather than assumed.
