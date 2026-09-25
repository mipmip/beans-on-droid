## Purpose

The states the application as a whole can be in, and how someone moves between
its screens. It exists so that "loading", "empty" and "broken" are states the app
is designed to be in rather than gaps between the states it was designed for.

## ADDED Requirements

### Requirement: One observable application state

The system SHALL expose a single state describing what the app is currently
showing, which is exactly one of: no repository, working, ready with beans, or
failed with a reason.

#### Scenario: Nothing added yet

- **WHEN** the app starts with no repository configured
- **THEN** the state is no-repository

#### Scenario: Beans available

- **WHEN** the active repository has been cloned and indexed
- **THEN** the state is ready, carrying the repository and its beans

#### Scenario: Failure carries a reason

- **WHEN** cloning or indexing the active repository fails
- **THEN** the state is failed and carries the reason

### Requirement: Adding a repository clones and indexes it

The system SHALL clone a newly added repository, make it active and index it,
and SHALL leave no entry in the list when the clone fails.

#### Scenario: Successful add

- **WHEN** a reachable repository containing beans is added
- **THEN** it becomes the active repository
- **AND** the state becomes ready with its beans

#### Scenario: Clone fails

- **WHEN** adding a repository that cannot be cloned
- **THEN** the repository is not in the list
- **AND** the state is failed

#### Scenario: Cloned repository has no beans

- **WHEN** a repository clones but contains no bean directory
- **THEN** the state is failed with a not-a-beans-repository reason

### Requirement: Skipped files are surfaced, not hidden

The system SHALL report the bean files it could not parse alongside the ones it
could, rather than discarding them silently.

#### Scenario: Repository with one unparseable file

- **WHEN** a repository holds valid bean files and one that cannot be parsed
- **THEN** the state is ready with the valid beans
- **AND** the unparseable file is listed as skipped

### Requirement: Switching and removing repositories

The system SHALL reindex when the active repository changes, and SHALL choose
another repository when the active one is removed.

#### Scenario: Switching

- **WHEN** a different repository is activated
- **THEN** the state becomes ready with that repository's beans

#### Scenario: Removing the active repository with others left

- **WHEN** the active repository is removed and another remains
- **THEN** the state becomes ready with the remaining repository

#### Scenario: Removing the last repository

- **WHEN** the only repository is removed
- **THEN** the state is no-repository

### Requirement: The clone URL is validated before anything happens

The system SHALL reject a clone URL that is empty, uses SSH, or does not use
HTTP or HTTPS, and SHALL explain why without contacting the network.

#### Scenario: SSH URL

- **WHEN** an SSH URL is submitted
- **THEN** the form reports that SSH is not supported and suggests the HTTPS URL
- **AND** no repository is added

#### Scenario: Empty URL

- **WHEN** the URL is blank
- **THEN** the form asks for a clone URL

#### Scenario: Correcting the URL

- **WHEN** the URL is edited after an error
- **THEN** the error is cleared

### Requirement: Navigation

The system SHALL provide a bean list, a bean detail view reachable from it, and
a repository screen, and SHALL let the user return from each.

#### Scenario: Opening a bean

- **WHEN** a bean is chosen from the list
- **THEN** its detail view is shown

#### Scenario: Returning

- **WHEN** the user goes back from a detail view
- **THEN** the list is shown again

#### Scenario: Reaching the repositories

- **WHEN** the user opens the repository screen from the list
- **THEN** the repository screen is shown
