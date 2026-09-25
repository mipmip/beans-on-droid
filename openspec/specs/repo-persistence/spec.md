# repo-persistence Specification

## Purpose
Remembering the repositories someone added to the app, which one they are
looking at, and the tokens that let the app read the private ones. It exists so
that a credential's storage rules are stated and testable rather than implied by
whichever file happens to hold it.

## Requirements

### Requirement: The repository list survives restarts

The system SHALL persist the repositories a user adds, with their URL and label,
and SHALL make the list observable so the interface updates when it changes.

#### Scenario: Added repository is still there later

- **WHEN** a repository is added and the app is restarted
- **THEN** the repository is in the list with the same URL and label

#### Scenario: List is observable

- **WHEN** a repository is added while the list is being observed
- **THEN** the observer receives the updated list without being asked again

#### Scenario: Adding the same URL twice

- **WHEN** a URL that is already in the list is added again
- **THEN** the list still contains one entry for that URL

### Requirement: Exactly one repository is active

The system SHALL track which repository is active, SHALL make the first added
repository active, and SHALL never report more than one as active.

#### Scenario: First repository becomes active

- **WHEN** the first repository is added to an empty list
- **THEN** it is the active repository

#### Scenario: Switching

- **WHEN** a different repository is made active
- **THEN** it is active and the previous one is not

#### Scenario: Removing the active repository

- **WHEN** the active repository is removed and others remain
- **THEN** one of the remaining repositories is active

#### Scenario: Removing the last repository

- **WHEN** the only repository is removed
- **THEN** no repository is active

### Requirement: Tokens are encrypted at rest

The system SHALL encrypt a token with a key held in the device's hardware-backed
key store before writing it, and SHALL NOT write the token in plain text.

#### Scenario: Stored token round-trips

- **WHEN** a token is stored for a repository and read back
- **THEN** the value read equals the value stored

#### Scenario: Stored form is not the token

- **WHEN** a token is stored
- **THEN** the persisted bytes do not contain the token's plain text

#### Scenario: Each token is independent

- **WHEN** tokens are stored for two repositories
- **THEN** reading one returns its own token

#### Scenario: Absent token

- **WHEN** a repository has no token
- **THEN** reading its token yields nothing rather than an error

### Requirement: Removing a repository removes its secrets

The system SHALL delete a repository's token when the repository is removed.

#### Scenario: Token goes with the repository

- **WHEN** a repository with a token is removed
- **THEN** reading a token for that repository yields nothing

#### Scenario: Other tokens are untouched

- **WHEN** one of two repositories with tokens is removed
- **THEN** the other repository's token is unchanged

### Requirement: Tokens do not leak through diagnostics

The system SHALL keep token values out of log output and out of the text
representation of any value that holds one.

#### Scenario: Rendering a repository that has a token

- **WHEN** a repository configuration is converted to text
- **THEN** the text does not contain the token
