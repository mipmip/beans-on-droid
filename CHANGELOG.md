# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- A failed refresh replaced the beans already on screen with an error page,
  instead of keeping the last fetched copy.
- Recreating a screen after a failed refresh silently cleared the warning that
  the beans were stale.
- The bean detail view claimed a bean was missing whenever the index was not
  loaded, including before any repository was opened.

- The coverage gate measured only the two core packages, so it reported
  success while overall coverage was below its own floor.

### Added

- Project scaffolding: OpenSpec, beans, nix flake, quality gate, ship script.
- Reproducible nix dev shell with JDK 17, Android SDK 37 and Gradle.
- Android app skeleton: Kotlin, Compose, Material 3, minSdk 26, that builds
  and launches.
- Coverage gate: 70 percent overall and 80 percent on the bean and index
  packages, enforced before every ship.
- Documented bean file format in docs/bean-format.md, with parser fixtures
  taken from the real hmans/beans repository.
- Bean parser: reads every documented frontmatter field, keeps unrecognised
  ones, and skips a malformed file instead of failing the whole repository.
- Bean index: filter by status, type and tag, text search over title, body and
  id, and relationship resolution in both directions.
- Repository store: shallow clone over HTTPS with an optional token, refresh as
  fetch plus hard reset, delete, and failures reported as authentication,
  network or not-a-beans-repository.
- Repository list persisted in DataStore, with tokens encrypted by an
  Android Keystore key and removed together with their repository.
- App shell: one observable application state, navigation between the bean
  list, a bean's detail and the repository screen, and clone URL validation.
- Repository screen: add by HTTPS URL with an optional token, switch between
  repositories, and remove one after a confirmation.
- Bean list: search over title, body and id, filters built from the statuses,
  types and tags the repository actually uses, and pull to refresh.
- Bean detail: rendered Markdown body, every frontmatter field including
  unrecognised ones, and tappable links to related beans.
- Files the parser could not read are listed by name with a reason, not just
  counted.
