## 1. Markdown

- [x] 1.1 Add Markwon core, tables, strikethrough and task list extensions
- [x] 1.2 `MarkdownText` composable wrapping a `TextView`, themed and remembered

## 2. Screen

- [x] 2.1 Header with title, id and status, type and archived pills
- [x] 2.2 Frontmatter rows, omitting fields the bean does not set
- [x] 2.3 Unrecognised fields under their own heading
- [x] 2.4 Relationship links for parent, children, blocking and blocked-by
- [x] 2.5 Unresolved relationship ids shown as plain text
- [x] 2.6 Rendered Markdown body, omitted when the body is empty
- [x] 2.7 Message and a way back when the bean id is not in the repository

## 3. Tests

- [x] 3.1 Instrumented: frontmatter fields shown
- [x] 3.2 Instrumented: unrecognised fields shown raw
- [x] 3.3 Instrumented: Markdown rendered, asserted through the view hierarchy
- [x] 3.4 Instrumented: relationships listed and tappable
- [x] 3.5 Instrumented: children derived on the parent
- [x] 3.6 Instrumented: dangling reference shown without a link
- [x] 3.7 Instrumented: unknown bean id degrades gracefully
