## ADDED Requirements

### Requirement: The running build identifies itself

The system SHALL show the application's version name and version code on the
repository screen, and SHALL do so whether or not any repository is configured.

The version code SHALL be shown alongside the version name, because that is the
number a store listing and a bug report are ordered by.

#### Scenario: With repositories configured

- **WHEN** the repository screen is shown with repositories in the list
- **THEN** the version name and version code are visible

#### Scenario: With no repositories

- **WHEN** the repository screen is shown before any repository has been added
- **THEN** the version name and version code are still visible

#### Scenario: The version matches the build

- **WHEN** the displayed version is compared with the installed package
- **THEN** both the name and the code are those of the running build, not a
  value written separately by hand
