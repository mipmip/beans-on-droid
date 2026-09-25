## Purpose

The screen where someone adds a repository, chooses which one they are reading,
and removes one they no longer want. It is the only place in the app that takes a
credential, so what it shows and what it hides are part of the contract.

## ADDED Requirements

### Requirement: The repository list shows its state

The system SHALL list every configured repository with its name and URL, SHALL
mark which one is active, and SHALL indicate which ones have a stored token
without revealing it.

#### Scenario: Active repository is marked

- **WHEN** repositories are listed
- **THEN** exactly one is shown as active

#### Scenario: Repository with a token

- **WHEN** a repository was added with a token
- **THEN** it is marked as using a token
- **AND** the token itself is not shown

#### Scenario: No repositories

- **WHEN** no repository has been added
- **THEN** the screen explains that one can be added with an HTTPS clone URL and
  that private repositories also need a token

### Requirement: Adding a repository

The system SHALL offer a form for a clone URL, an optional name and an optional
token, SHALL mask the token as it is typed, and SHALL add the repository when the
URL is valid.

#### Scenario: Valid repository

- **WHEN** a reachable repository's URL is submitted with a name
- **THEN** it appears in the list under that name
- **AND** it becomes the active repository

#### Scenario: Token is masked

- **WHEN** a token is typed into the form
- **THEN** its characters are not displayed

### Requirement: The URL is checked before the network

The system SHALL reject an invalid clone URL with an explanation and SHALL NOT
contact the network or add a repository in that case.

#### Scenario: SSH URL

- **WHEN** an SSH URL is submitted
- **THEN** the form explains that SSH is not supported and to use the HTTPS URL
- **AND** no repository is added

### Requirement: A clone in progress is visible

The system SHALL indicate that a clone is running and SHALL prevent the form
being submitted or dismissed again until it finishes.

#### Scenario: While cloning

- **WHEN** a clone is running
- **THEN** the screen says so
- **AND** the confirm and cancel actions are unavailable

#### Scenario: After a failed clone

- **WHEN** a clone fails
- **THEN** the reason is shown in the form
- **AND** the URL that was typed is still there to correct

### Requirement: Switching the active repository

The system SHALL make a repository active when it is chosen, and the beans shown
elsewhere SHALL be that repository's.

#### Scenario: Choosing another repository

- **WHEN** a repository that is not active is chosen
- **THEN** it becomes the active one

### Requirement: Removing a repository is confirmed

The system SHALL ask for confirmation before removing a repository and SHALL say
that the local copy and any token are deleted while the server is untouched.

#### Scenario: Cancelling

- **WHEN** removal is started and then cancelled
- **THEN** the repository is still listed

#### Scenario: Confirming

- **WHEN** removal is confirmed
- **THEN** the repository is no longer listed
