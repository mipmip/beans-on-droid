# bean-index Specification

## Purpose
Answering the questions the screens ask about a repository's beans: which ones
match a filter, which ones match a search, and which beans a given bean is
related to. It exists so that the rules for filtering, ordering and relationship
resolution live in one tested place instead of in each view model.

## Requirements

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

The system SHALL order results by the same comparator the beans tool uses, in
this precedence: configured status order, then manual `order`, then configured
priority order, then configured type order, then title without regard to case.

A bean that declares an `order` SHALL come before one that does not. A bean with
no priority SHALL be ordered as though its priority were `normal`. A status,
priority or type value that is not one of the configured values SHALL sort last
within its own category rather than being dropped or treated as absent.

#### Scenario: Status is the primary key

- **WHEN** an `in-progress` bean and a `todo` bean are ordered
- **THEN** the `in-progress` bean comes first, whatever their other fields say

#### Scenario: Ordered beans

- **WHEN** two `todo` beans have orders `Vy` and `zzzV`
- **THEN** the one with `Vy` comes first

#### Scenario: Missing order

- **WHEN** two `todo` beans are ordered and only one declares an `order`
- **THEN** the one with an `order` comes first

#### Scenario: Tie on order

- **WHEN** two beans share a status and the same `order`
- **THEN** priority decides, and where priority is equal too, title decides

#### Scenario: Priority orders beans that have no manual order

- **WHEN** two `todo` beans without an `order` have priorities `high` and `low`
- **THEN** the `high` one comes first

#### Scenario: Absent priority counts as normal

- **WHEN** one bean declares `low` and another declares no priority
- **THEN** the bean with no priority comes first

#### Scenario: Type breaks a priority tie

- **WHEN** two beans match on status, order and priority and are a `bug` and a
  `task`
- **THEN** the `bug` comes first

#### Scenario: Title breaks a type tie, ignoring case

- **WHEN** two beans match on every earlier key and are titled `apple` and
  `Banana`
- **THEN** `apple` comes first

#### Scenario: An unrecognised status sorts last

- **WHEN** a bean declares a status that is not configured
- **THEN** it is ordered after every bean with a configured status
- **AND** it is still present in the result

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

### Requirement: Results can be sorted by a chosen field

The system SHALL accept a sort field as part of a query and SHALL order results
by it. The available fields are the default comparator, creation time, update
time, status, priority, type, title and id.

A query that does not name a sort field SHALL use the default comparator.

#### Scenario: Sorting by update time

- **WHEN** a query sorts by update time
- **THEN** results are ordered by their `updated_at` value

#### Scenario: A bean missing the sorted field

- **WHEN** a query sorts by creation time and a bean has no `created_at`
- **THEN** that bean appears after every bean that has one
- **AND** it is still present in the result

#### Scenario: Ties are resolved consistently

- **WHEN** two beans hold the same value for the sorted field
- **THEN** they are ordered by id, so the result is stable across refreshes

#### Scenario: Enumerated fields use their configured order

- **WHEN** a query sorts by priority
- **THEN** `critical` precedes `high`, which precedes `normal`, rather than the
  values being compared alphabetically

### Requirement: Sort direction can be reversed

The system SHALL accept a direction with the sort field and SHALL reverse the
ordering when descending is asked for. Time fields SHALL default to descending
and every other field SHALL default to ascending.

#### Scenario: Newest first by default

- **WHEN** a query sorts by update time without naming a direction
- **THEN** the most recently updated bean comes first

#### Scenario: Reversing a field

- **WHEN** a query sorts by title descending
- **THEN** results run from the last title to the first

#### Scenario: Beans missing the field stay last

- **WHEN** a query sorts by creation time descending and a bean has no
  `created_at`
- **THEN** that bean is still ordered after every bean that has one

### Requirement: Results can be returned as a tree

The system SHALL be able to return query results arranged by parentage: every
matching bean whose parent is not itself in the result appears at the top level,
and every matching bean whose parent is in the result appears beneath it.

Beans at each level SHALL be ordered by the query's ordering. The system SHALL
produce a finite result even when the parent relationships form a cycle.

#### Scenario: A child is nested under its parent

- **WHEN** a tree is built from a parent and its two children
- **THEN** the parent is at the top level with both children beneath it

#### Scenario: Beans with no parent sit at the top level

- **WHEN** the result contains beans with a parent and beans without one
- **THEN** those without a parent appear at the top level alongside the parents

#### Scenario: Ordering applies within each level

- **WHEN** a parent has an `in-progress` child and a `todo` child
- **THEN** the `in-progress` child is listed first

#### Scenario: Deeper nesting

- **WHEN** a bean's parent itself declares a parent
- **THEN** all three appear, each nested one level below the previous

#### Scenario: A cycle does not hang

- **WHEN** two beans each declare the other as their parent
- **THEN** a finite tree is returned containing both beans

### Requirement: Excluded ancestors are reported as context

When a matching bean's parent is not in the result, the system SHALL report that
parent alongside the tree, marked as context rather than as a match, so a caller
can show the grouping without counting it.

#### Scenario: Filter excludes a parent whose child matches

- **WHEN** a filter excludes an `in-progress` parent and keeps its `todo` child
- **THEN** the child is nested under that parent
- **AND** the parent is marked as context, not as a match

#### Scenario: The match count ignores context

- **WHEN** one parent is context and two children match
- **THEN** the number of matches is two

#### Scenario: A parent that is absent from the repository

- **WHEN** a matching bean names a parent that no bean in the repository has
- **THEN** the bean appears at the top level rather than under a fabricated
  parent
