## Purpose

The screen that shows a repository's beans and helps someone find one among
several hundred. It defines what a bean looks like in a list, how the list is
narrowed, and how the difference between "nothing here" and "nothing matched" is
communicated.

## ADDED Requirements

### Requirement: Every bean is listed with its identity

The system SHALL show each bean's id, title, status and type, and SHALL mark a
bean that came from the archive.

#### Scenario: A repository's beans

- **WHEN** a repository with beans is active
- **THEN** each bean's id, title, status and type are shown

#### Scenario: Untitled bean

- **WHEN** a bean has no title
- **THEN** it is still listed, marked as untitled

### Requirement: Search over title, body and id

The system SHALL narrow the list to beans matching a typed term, matching
against title, body and id without regard to case.

#### Scenario: Term in a title

- **WHEN** a term matching one bean's title is typed
- **THEN** only that bean is listed

#### Scenario: Term in a body

- **WHEN** a term appearing only in one bean's body is typed
- **THEN** that bean is listed

#### Scenario: Clearing the search

- **WHEN** the search field is emptied
- **THEN** every bean is listed again

### Requirement: Filters come from the repository

The system SHALL offer filters for the statuses, types and tags that occur in
the indexed beans, rather than a fixed list, and SHALL combine them with the
search.

#### Scenario: Filtering by type

- **WHEN** a type filter is chosen
- **THEN** only beans of that type are listed

#### Scenario: Two facets together

- **WHEN** both a type and a status filter are chosen
- **THEN** only beans matching both are listed

#### Scenario: Filtering by tag

- **WHEN** a tag filter is chosen
- **THEN** only beans carrying that tag are listed

#### Scenario: Clearing filters

- **WHEN** the filters are cleared
- **THEN** every bean is listed again and the search term is kept

### Requirement: The count is visible

The system SHALL show how many beans match out of the repository's total, and
SHALL report how many files could not be read.

#### Scenario: Filtered list

- **WHEN** a filter reduces three beans to one
- **THEN** the screen shows one of three

#### Scenario: Unreadable files

- **WHEN** the repository holds files the parser skipped
- **THEN** the screen says how many could not be read

### Requirement: Pull to refresh

The system SHALL refresh the repository when the list is pulled down, and SHALL
show that a refresh is running.

#### Scenario: Refreshing

- **WHEN** the list is pulled down
- **THEN** the repository is fetched and reindexed
- **AND** the list reflects the new commit

### Requirement: Empty and no-match are different

The system SHALL distinguish a repository with no beans from a search that
matched nothing, and SHALL offer an action suited to each.

#### Scenario: Repository with no beans

- **WHEN** the active repository's bean directory is empty
- **THEN** the screen says the repository has no beans and offers the repository
  list

#### Scenario: Search matched nothing

- **WHEN** a search or filter excludes every bean
- **THEN** the screen says nothing matches and offers to clear the filters

#### Scenario: No repository at all

- **WHEN** no repository has been added
- **THEN** the screen offers to add one

### Requirement: Opening a bean

The system SHALL open a bean's detail view when it is chosen.

#### Scenario: Choosing a bean

- **WHEN** a bean in the list is chosen
- **THEN** its detail view is opened
