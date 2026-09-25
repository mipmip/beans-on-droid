# end-to-end Specification

## Purpose
The journeys someone takes through the whole app, verified against a real git
repository served over HTTP rather than against components in isolation. It
exists because every layer of this app can be correct while the app is not.

## Requirements

### Requirement: Adding a repository over HTTP reaches a populated list

The system SHALL let a person add a repository by its HTTP clone URL and arrive
at a list of that repository's beans.

#### Scenario: From empty install to beans

- **WHEN** a repository served over HTTP is added from the repository screen
- **THEN** it appears in the repository list as active
- **AND** returning to the bean list shows that repository's beans

### Requirement: Search and filters work against a cloned repository

The system SHALL narrow the list by a search term and by a type filter when
driven through the interface.

#### Scenario: Searching body text

- **WHEN** a term that appears only in one bean's body is typed
- **THEN** only that bean is listed

#### Scenario: Filtering by type after clearing the search

- **WHEN** the search is cleared and a type filter is applied
- **THEN** only beans of that type are listed

### Requirement: Relationship links navigate between beans

The system SHALL open the related bean when a relationship link is followed, and
the destination SHALL show the inverse relationship.

#### Scenario: Child to parent and back again

- **WHEN** a bean's parent link is followed
- **THEN** the parent's detail view is shown
- **AND** it lists the original bean as a child

### Requirement: Pull to refresh fetches new commits

The system SHALL fetch and reindex when the bean list is pulled down, and beans
added upstream since the clone SHALL appear.

#### Scenario: A commit added after cloning

- **WHEN** a bean is committed upstream after the repository was cloned
- **AND** the bean list is pulled down
- **THEN** the new bean is listed
- **AND** the count reflects it

### Requirement: Authentication failures reach the user

The system SHALL report an authentication failure when a repository requiring
credentials is added with a token the server rejects, and SHALL succeed when the
token is accepted.

#### Scenario: Wrong token

- **WHEN** a repository requiring a token is added with the wrong one
- **THEN** the form says it could not sign in and mentions the access token

#### Scenario: Right token

- **WHEN** the same repository is added with the correct token
- **THEN** the clone succeeds and its beans are listed
