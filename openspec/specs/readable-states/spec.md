# readable-states Specification

## Purpose
What the app shows when there is nothing to show, when it is busy, and when
something failed. These are the states a read-only client spends real time in,
because it depends on a network and on someone else's repository, so each one is
specified rather than left to whatever the happy path degrades into.

## Requirements

### Requirement: Three kinds of empty are distinguished

The system SHALL distinguish having no repository, having a repository with no
beans, and having a filter that matches nothing, and SHALL offer an action
appropriate to each.

#### Scenario: No repository

- **WHEN** no repository has been added
- **THEN** the screen offers to add one

#### Scenario: Repository with no beans

- **WHEN** the active repository's bean directory exists but holds no bean files
- **THEN** the screen says the repository has no beans

#### Scenario: Filter matches nothing

- **WHEN** a search excludes every bean
- **THEN** the screen says nothing matches and offers to clear the filters

### Requirement: Work in progress is named

The system SHALL say which operation is running while it runs, distinguishing a
first clone, a refresh and reading the bean files.

#### Scenario: Cloning

- **WHEN** a repository is being cloned for the first time
- **THEN** the screen says it is cloning

#### Scenario: Reading

- **WHEN** the bean files are being parsed
- **THEN** the screen says it is reading beans

### Requirement: Failures say which failure it was

The system SHALL give an authentication failure, a network failure and a
repository with no bean directory distinct messages, and SHALL offer a retry on
each.

#### Scenario: Unreachable repository

- **WHEN** the active repository cannot be reached
- **THEN** the screen says it could not be reached and offers to try again

#### Scenario: Repository without beans

- **WHEN** the active repository has no bean directory
- **THEN** the screen says the repository has no beans in it and offers to try
  again

### Requirement: A failed refresh keeps what is already shown

The system SHALL continue to show the beans from the last successful fetch when
a refresh fails, SHALL mark them as stale with the reason, and SHALL offer a
retry.

#### Scenario: Refresh fails with beans on screen

- **WHEN** a refresh of the active repository fails while its beans are shown
- **THEN** those beans are still listed
- **AND** a message says the copy is the last fetched one, with the reason
- **AND** a retry is offered

#### Scenario: Refresh succeeds after failing

- **WHEN** a refresh succeeds after a previous one failed
- **THEN** the stale marker is gone

#### Scenario: Nothing was on screen

- **WHEN** loading the active repository fails and nothing was shown before
- **THEN** the screen shows the failure rather than an empty list

### Requirement: A stale marker survives the screen being recreated

The system SHALL NOT reindex a repository that is already being shown when a
screen is recreated, so that a stale marker is not silently cleared.

#### Scenario: Screen recreated after a failed refresh

- **WHEN** the screen is recreated while showing stale beans
- **THEN** the beans and the stale marker are still shown

### Requirement: Unreadable files can be inspected

The system SHALL report how many bean files could not be parsed and SHALL list
them by name with the reason for each on request.

#### Scenario: Repository with an unparseable file

- **WHEN** a repository holds a bean file whose frontmatter cannot be parsed
- **THEN** the screen reports one file that could not be read
- **AND** opening that report names the file and gives a reason
- **AND** the beans that could be read are still listed

### Requirement: A detail view without an index says so

The system SHALL distinguish a bean that is absent from the repository from an
index that has not finished loading or has failed.

#### Scenario: No repository open

- **WHEN** a detail view is opened while no repository is open
- **THEN** the screen says it is not ready rather than that the bean is missing

#### Scenario: Bean genuinely absent

- **WHEN** a detail view is opened for an id the loaded repository does not have
- **THEN** the screen says that bean is not in this repository
