## ADDED Requirements

### Requirement: An id can be tested for membership

The system SHALL answer, for any id, whether it holds a bean with that id, and
SHALL give that bean when it does, without requiring the caller to declare a
relationship.

#### Scenario: An id the index holds

- **WHEN** an id that a parsed bean carries is looked up
- **THEN** the index reports it as present and gives that bean

#### Scenario: An id no bean has

- **WHEN** an id that no bean in the repository carries is looked up
- **THEN** the index reports it as absent
- **AND** no error is raised
