#!/usr/bin/env bash
# gate.sh
#
# The quality gate for this project. Run on its own, or via ship-change.sh.
#
#   1. nix flake check         always
#   2. gradle build/test/lint/coverage  as soon as ./gradlew exists
#
# The gradle half runs inside `nix develop` so the JDK and Android SDK come
# from the flake rather than from whatever happens to be on PATH. Gradle needs
# network access to resolve dependencies, which a pure nix derivation does not
# get, so the build deliberately lives here and not in `checks`.
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

echo "==> gate [1/2] nix flake check"
nix flake check

if [[ ! -x ./gradlew ]]; then
  echo "==> gate [2/2] skipped: no ./gradlew yet (Gradle skeleton not built)"
  echo "==> gate passed (flake only)"
  exit 0
fi

echo "==> gate [2/2] gradle build, test, lint, coverage"
nix develop --command ./gradlew \
  assembleDebug \
  test \
  lint \
  jacocoCoverageVerification

echo "==> gate passed"
