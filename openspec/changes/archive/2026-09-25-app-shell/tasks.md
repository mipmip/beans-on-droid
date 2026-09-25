## 1. Loading

- [x] 1.1 `BeanLoader` producing an index plus the skipped files
- [x] 1.2 Tests: real fixtures, skipped files, archived beans, empty directory

## 2. Coordinator

- [x] 2.1 Extract `RepoCatalog` and make `RepoRegistry` implement it
- [x] 2.2 `IndexState` with no-repository, working, ready and failed
- [x] 2.3 `BeansRepository.addRepository`, removing the entry when the clone fails
- [x] 2.4 `loadActive`, cloning first when the working copy is missing
- [x] 2.5 `activate`, `removeRepository`, `refresh`
- [x] 2.6 Tests against a local git repository and a fake catalog

## 3. View model

- [x] 3.1 `AppViewModel` exposing index state, repositories, query, form and
      refresh flag
- [x] 3.2 Query mutators for term, status, type, tag, archived and clear
- [x] 3.3 Add-repo form with validation, busy state and error
- [x] 3.4 `activate`, `removeRepository`, `refresh`, `retry`
- [x] 3.5 Human-readable text for each error category
- [x] 3.6 Tests with a test dispatcher

## 4. URL validation

- [x] 4.1 `RepoUrl.validate` rejecting empty, SSH and non-HTTP URLs
- [x] 4.2 Tests for each case

## 5. Shell

- [x] 5.1 Navigation Compose with bean list, detail and repository routes
- [x] 5.2 Stub screens for all three
- [x] 5.3 Application creates the `BeansRepository`; the activity reads it
- [x] 5.4 `assembleDebug` and `lint` stay clean
