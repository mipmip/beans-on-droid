## 1. Screen

- [x] 1.1 List repositories with name, URL, active marker and token marker
- [x] 1.2 Empty state explaining how to add one
- [x] 1.3 Add button opening the form
- [x] 1.4 Form with clone URL, optional name and masked token
- [x] 1.5 Inline validation error from the view model
- [x] 1.6 Progress indication while cloning, with the actions disabled
- [x] 1.7 Tapping a repository activates it
- [x] 1.8 Remove with a confirmation that states what is deleted

## 2. Accessibility

- [x] 2.1 Content descriptions on every control the user can act on

## 3. Tests

- [x] 3.1 Instrumented: empty state
- [x] 3.2 Instrumented: SSH URL rejected and nothing added
- [x] 3.3 Instrumented: adding a real local repository lists it and marks it
      active
- [x] 3.4 Instrumented: removal asks first, cancels, then removes
- [x] 3.5 Instrumented: a repository with a token is marked
- [x] 3.6 Shared test helpers for building local repositories and beans
