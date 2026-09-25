## 1. List

- [x] 1.1 Row showing id, title, status, type and an archived marker
- [x] 1.2 Status and type pills coloured to match the beans tool
- [x] 1.3 Top bar showing the active repository's name
- [x] 1.4 Opening a bean navigates to its detail view

## 2. Search and filters

- [x] 2.1 Search field over title, body and id, with a clear button
- [x] 2.2 Filter chips built from the facets present in the data
- [x] 2.3 Archived toggle and a clear-filters chip
- [x] 2.4 Matching count out of the total
- [x] 2.5 Count of files that could not be read

## 3. Refresh

- [x] 3.1 Pull to refresh calling the repository refresh
- [x] 3.2 Refresh indicator driven by the view model

## 4. States

- [x] 4.1 No repository, with an action to add one
- [x] 4.2 Repository with no beans, with an action to open the repository list
- [x] 4.3 Search matching nothing, with an action to clear the filters
- [x] 4.4 Working and failed states

## 5. Tests

- [x] 5.1 Instrumented: no repository
- [x] 5.2 Instrumented: beans listed with id, status and type
- [x] 5.3 Instrumented: search by title and by body
- [x] 5.4 Instrumented: search with no match, then cleared
- [x] 5.5 Instrumented: filter by type, by type and status, and by tag
- [x] 5.6 Instrumented: tapping a bean opens it
