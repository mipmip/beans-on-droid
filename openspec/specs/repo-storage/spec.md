# repo-storage Specification

## Purpose
Getting a copy of someone's git repository onto the phone and keeping it
current, without ever writing to it. This capability owns what "up to date"
means for a read-only mirror and how a failure to reach a repository is
described to the person who typed the URL.

## Requirements

### Requirement: Cloning a repository

The system SHALL clone a repository from an HTTPS URL into storage private to
the app, fetching only the most recent commit.

#### Scenario: Public repository

- **WHEN** a valid HTTPS URL for a reachable repository is added
- **THEN** a working copy exists locally
- **AND** it contains the repository's files at the remote branch's tip

#### Scenario: Only the latest commit is fetched

- **WHEN** a repository with several commits is cloned
- **THEN** the local copy has a single commit of history

#### Scenario: Private repository with a token

- **WHEN** a URL requiring authentication is added with a valid token
- **THEN** the clone succeeds

#### Scenario: Two repositories do not collide

- **WHEN** two different URLs are added
- **THEN** each has its own working directory and neither affects the other

### Requirement: Refreshing is a hard reset

The system SHALL refresh a repository by fetching the remote branch and resetting
the working copy to it, discarding any local difference, and SHALL never merge.

#### Scenario: New commit upstream

- **WHEN** the remote gains a commit and the repository is refreshed
- **THEN** the working copy matches the new remote tip

#### Scenario: Remote history was rewritten

- **WHEN** the remote branch is force-pushed to an unrelated commit and the
  repository is refreshed
- **THEN** the working copy matches the new remote tip rather than failing to
  merge

#### Scenario: Local file was modified

- **WHEN** a file in the working copy differs from the remote and the repository
  is refreshed
- **THEN** the file matches the remote again

### Requirement: Deleting a repository

The system SHALL remove a repository's working copy from storage on request.

#### Scenario: Delete removes the files

- **WHEN** a cloned repository is deleted
- **THEN** its working directory no longer exists

#### Scenario: Delete leaves other repositories alone

- **WHEN** one of two repositories is deleted
- **THEN** the other is still present and usable

### Requirement: Locating the bean directory

The system SHALL determine a repository's bean directory from `.beans.yml` when
present, and SHALL otherwise use `.beans` at the repository root.

#### Scenario: Configured path

- **WHEN** `.beans.yml` sets the bean path to `issues`
- **THEN** the bean directory is `issues`

#### Scenario: Default path

- **WHEN** there is no `.beans.yml` but a `.beans` directory exists
- **THEN** the bean directory is `.beans`

#### Scenario: Repository is not a beans repository

- **WHEN** neither a configured bean path nor `.beans` exists
- **THEN** the result is a not-a-beans-repository failure

### Requirement: Failures are classified

The system SHALL report a failure as one of authentication, network,
not-a-beans-repository or unknown, each carrying a message, and SHALL NOT leave a
partial working copy behind after a failed clone.

#### Scenario: Wrong or missing credentials

- **WHEN** a repository requiring authentication is cloned without a usable token
- **THEN** the failure is an authentication failure

#### Scenario: Host cannot be reached

- **WHEN** the remote host does not resolve or refuses the connection
- **THEN** the failure is a network failure

#### Scenario: Failed clone leaves nothing behind

- **WHEN** a clone fails for any reason
- **THEN** no working directory for that repository remains
