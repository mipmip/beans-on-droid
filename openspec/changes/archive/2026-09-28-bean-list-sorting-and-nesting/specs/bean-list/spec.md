## MODIFIED Requirements

### Requirement: Every bean is listed with its identity

The system SHALL show each bean's id, title, status, type and priority, and
SHALL mark a bean that came from the archive.

Priority SHALL be shown only when it is not `normal`, so that the common case
adds no noise, and SHALL distinguish a raised priority from a lowered one.

#### Scenario: A repository's beans

- **WHEN** a repository with beans is active
- **THEN** each bean's id, title, status and type are shown

#### Scenario: A bean with a raised priority

- **WHEN** a bean's priority is `critical` or `high`
- **THEN** the row shows that it is raised

#### Scenario: A bean with a lowered priority

- **WHEN** a bean's priority is `low` or `deferred`
- **THEN** the row shows that it is lowered

#### Scenario: A bean with normal or no priority

- **WHEN** a bean's priority is `normal` or absent
- **THEN** no priority marking is shown

#### Scenario: Untitled bean

- **WHEN** a bean has no title
- **THEN** it is still listed, marked as untitled

## ADDED Requirements

### Requirement: The list can be sorted

The system SHALL offer a sort control listing the default order, creation time,
update time, status, priority, type, title and id, together with a direction,
and SHALL reorder the list when one is chosen.

The control SHALL show which sort is active, so the current order is never
unexplained.

#### Scenario: Choosing a sort

- **WHEN** sorting by title is chosen
- **THEN** the list is ordered by title

#### Scenario: Reversing the direction

- **WHEN** the direction is reversed
- **THEN** the list order is reversed

#### Scenario: The active sort is visible

- **WHEN** a sort other than the default is active
- **THEN** the screen shows which one

#### Scenario: Returning to the default

- **WHEN** the default order is chosen again
- **THEN** the list returns to the default comparator's order

### Requirement: The sort choice is remembered per repository

The system SHALL persist the chosen sort and direction for each repository, and
SHALL restore them when that repository is opened again.

#### Scenario: Reopening a repository

- **WHEN** a sort is chosen and the app is restarted
- **THEN** that repository's list uses the same sort

#### Scenario: Two repositories

- **WHEN** two repositories are sorted differently
- **THEN** switching between them restores each one's own sort

### Requirement: The list nests children under their parents

Under the default order the system SHALL group the list by parentage: beans with
no parent among the results at the top level, their children indented beneath
them, ordered by the default comparator within each level.

#### Scenario: A parent and its children

- **WHEN** the repository holds a parent with two children
- **THEN** the children are shown indented under the parent

#### Scenario: Beans without a parent

- **WHEN** the repository holds beans with no parent
- **THEN** they appear at the top level among the parents rather than in a
  separate section

#### Scenario: Opening a nested bean

- **WHEN** a child row is chosen
- **THEN** its detail view opens, as for any other row

### Requirement: Sorting and searching flatten the list

The system SHALL show a flat list whenever a sort other than the default is
active or a search term has been entered, because a chosen order and a tree
describe different arrangements of the same rows.

#### Scenario: An explicit sort flattens

- **WHEN** sorting by update time is chosen
- **THEN** the list is flat, with no indentation

#### Scenario: A search flattens

- **WHEN** a search term is entered while the default order is active
- **THEN** the results are flat

#### Scenario: Clearing the search restores the tree

- **WHEN** the search term is cleared and the default order is active
- **THEN** the list is nested again

### Requirement: A filtered-out parent is kept as context

When a filter excludes a parent whose children still match, the system SHALL
keep the parent visible as a context row, distinguishable from a matching row,
so the grouping is preserved.

A context row SHALL NOT be included in the count of matching beans, and SHALL
still open its own detail view when chosen.

#### Scenario: Parent excluded, child matches

- **WHEN** a filter excludes a parent and keeps one of its children
- **THEN** the parent is shown as context with the child nested beneath it

#### Scenario: Context is not counted

- **WHEN** one parent is context and two of its children match out of ten beans
- **THEN** the count reads two of ten

#### Scenario: Context rows are still openable

- **WHEN** a context row is chosen
- **THEN** that bean's detail view opens

#### Scenario: No children match

- **WHEN** a filter excludes a parent and all of its children
- **THEN** neither the parent nor its children are shown
