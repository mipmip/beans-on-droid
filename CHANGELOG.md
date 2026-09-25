# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Project scaffolding: OpenSpec, beans, nix flake, quality gate, ship script.
- Reproducible nix dev shell with JDK 17, Android SDK 37 and Gradle.
- Android app skeleton: Kotlin, Compose, Material 3, minSdk 26, that builds
  and launches.
- Coverage gate: 70 percent overall and 80 percent on the bean and index
  packages, enforced before every ship.
