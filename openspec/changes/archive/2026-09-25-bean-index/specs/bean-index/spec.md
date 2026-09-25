## Purpose

Answering the questions the screens ask about a repository's beans: which ones
match a filter, which ones match a search, and which beans a given bean is
related to. It exists so that the rules for filtering, ordering and relationship
resolution live in one tested place instead of in each view model.

## ADDED Requirements

### Requirement: Filtering by status, type and tag

The system SHALL return the beans matching a query, where a filter with several
values for one facet matches any of them, and filters for different facets must
all match.

#### Scenario: Single status filter

- **WHEN** a query selects status `todo`
- **THEN** only beans whose status is `todo` are returned

#### Scenario: Several values for one facet

- **WHEN** a query selects statuses `todo` and `in-progress`
- **THEN** beans with either status are returned

#### Scenario: Different facets combine

- **WHEN** a query selects status `todo` and type `epic`
- **THEN** only beans that are both are returned

#### Scenario: Tag filter

- **WHEN** a query selects tag `integrations`
- **THEN** only beans carrying that tag are returned

#### Scenario: Empty query

- **WHEN** a query selects nothing
- **THEN** every non-archived bean is returned

### Requirement: Text search over title and body

The system SHALL match a search term against a bean's title and body, without
regard to case, and SHALL also match the bean's id.

#### Scenario: Match in the title

- **WHEN** the term appears in a bean's title in different case
- **THEN** that bean is returned

#### Scenario: Match in the body

- **WHEN** the term appears only in a bean's body
- **THEN** that bean is returned

#### Scenario: Match on id

- **WHEN** the term is a bean's id
- **THEN** that bean is returned

#### Scenario: Search combines with filters

- **WHEN** a query has both a term and a status filter
- **THEN** only beans matching both are returned

#### Scenario: No match

- **WHEN** no bean matches the term
- **THEN** an empty result is returned

### Requirement: Archived beans are excluded by default

The system SHALL exclude archived beans from results unless the query asks for
them.

#### Scenario: Default query

- **WHEN** a query does not ask for archived beans
- **THEN** no archived bean appears in the result

#### Scenario: Query including archived

- **WHEN** a query asks for archived beans
- **THEN** archived and active beans are both returned

### Requirement: Ordering

The system SHALL sort results by the `order` field lexicographically, place
beans without an `order` after those with one, and break ties by title.

#### Scenario: Ordered beans

- **WHEN** beans have orders `Vy` and `zzzV`
- **THEN** the one with `Vy` comes first

#### Scenario: Missing order

- **WHEN** one bean has an order and another has none
- **THEN** the one with an order comes first

#### Scenario: Tie on order

- **WHEN** two beans have the same order
- **THEN** they are returned in title order

### Requirement: Relationship resolution

The system SHALL resolve a bean's parent, children, blocking and blocked-by
relationships to the beans they name, and SHALL treat the explicit lists and the
inverse relations found by scanning as one set.

#### Scenario: Parent and children

- **WHEN** bean B declares bean A as its parent
- **THEN** A's children include B
- **AND** B's parent resolves to A

#### Scenario: Inverse blocking

- **WHEN** bean A declares `blocking: [B]` and B declares nothing
- **THEN** A's blocking includes B
- **AND** B's blocked-by includes A

#### Scenario: Mirrored declaration is not duplicated

- **WHEN** A declares `blocking: [B]` and B declares `blocked_by: [A]`
- **THEN** B's blocked-by contains A exactly once

#### Scenario: Reference to a bean that does not exist

- **WHEN** a bean names a relationship id that no bean in the set has
- **THEN** the id is reported as unresolved rather than omitted or faked

### Requirement: Facets present in the data

The system SHALL expose the distinct statuses, types and tags that occur in the
indexed beans.

#### Scenario: Facets from the indexed set

- **WHEN** the indexed beans use two statuses and three tags
- **THEN** exactly those two statuses and three tags are reported
- **AND** each list is sorted and free of duplicates
