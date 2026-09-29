#!/usr/bin/env bash
# release.sh <patch|minor|major> [--dry-run]
#
# Cuts one release:
#   bump VERSION -> roll the changelog -> write the fastlane changelog
#   -> commit -> tag -> push.
#
# This is not ship-change.sh. That lands one unit of work; this declares that
# a set of landed work exists for people.
set -euo pipefail

BUMP="${1:?usage: release.sh <patch|minor|major> [--dry-run]}"
DRY_RUN=0
[[ "${2:-}" == "--dry-run" ]] && DRY_RUN=1

case "$BUMP" in
  patch | minor | major) ;;
  *)
    echo "release: bump must be patch, minor or major, got '$BUMP'" >&2
    exit 1
    ;;
esac

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

VERSION_FILE="$ROOT/VERSION"
CHANGELOG="$ROOT/CHANGELOG.md"
FASTLANE_DIR="$ROOT/fastlane/metadata/android/en-US/changelogs"

[[ -f "$VERSION_FILE" ]] || { echo "release: no VERSION file" >&2; exit 1; }
[[ -f "$CHANGELOG" ]] || { echo "release: no CHANGELOG.md" >&2; exit 1; }

CURRENT="$(tr -d '[:space:]' < "$VERSION_FILE")"
[[ "$CURRENT" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || {
  echo "release: VERSION must be major.minor.patch, found '$CURRENT'" >&2
  exit 1
}

IFS=. read -r MAJOR MINOR PATCH <<< "$CURRENT"
case "$BUMP" in
  major) MAJOR=$((MAJOR + 1)); MINOR=0; PATCH=0 ;;
  minor) MINOR=$((MINOR + 1)); PATCH=0 ;;
  patch) PATCH=$((PATCH + 1)) ;;
esac
NEXT="${MAJOR}.${MINOR}.${PATCH}"
VERSION_CODE=$((MAJOR * 1000000 + MINOR * 10000 + PATCH * 100))
TAG="v${NEXT}"
TODAY="$(date +%Y-%m-%d)"

if [[ "$DRY_RUN" -eq 0 ]]; then
  if [[ -n "$(jj status --no-pager 2>/dev/null | grep -E '^[AMD] ' || true)" ]]; then
    echo "release: the working copy has changes; commit or discard them first" >&2
    exit 1
  fi
  if git rev-parse "$TAG" >/dev/null 2>&1; then
    echo "release: tag $TAG already exists" >&2
    exit 1
  fi
fi

# The section the changelog is about to gain, as plain text for fastlane.
NOTES="$(awk '
  /^## \[Unreleased\]/ { capture = 1; next }
  /^## \[/ { capture = 0 }
  capture { print }
' "$CHANGELOG" | sed -e 's/^### //' -e 's/^- /* /' | awk 'NF || p { print; p = 1 }')"

if [[ -z "$(echo "$NOTES" | tr -d '[:space:]')" ]]; then
  echo "release: [Unreleased] in CHANGELOG.md is empty; nothing to release" >&2
  exit 1
fi

echo "==> $CURRENT -> $NEXT  (versionCode $VERSION_CODE, tag $TAG)"
echo "==> files this release touches:"
echo "      VERSION"
echo "      CHANGELOG.md"
echo "      fastlane/metadata/android/en-US/changelogs/${VERSION_CODE}.txt"
echo "==> release notes:"
printf '      %s\n' "$NOTES"

if [[ "$DRY_RUN" -eq 1 ]]; then
  echo "==> dry run, nothing changed"
  exit 0
fi

echo "$NEXT" > "$VERSION_FILE"

python3 - "$CHANGELOG" "$NEXT" "$TODAY" <<'PY'
import sys
path, version, today = sys.argv[1], sys.argv[2], sys.argv[3]
text = open(path).read()
marker = "## [Unreleased]"
if marker not in text:
    raise SystemExit(f"release: {marker} not found in {path}")
text = text.replace(marker, f"{marker}\n\n## [{version}] - {today}", 1)
open(path, "w").write(text)
PY

mkdir -p "$FASTLANE_DIR"
printf '%s\n' "$NOTES" > "${FASTLANE_DIR}/${VERSION_CODE}.txt"

echo "==> commit and tag"
git add -A
jj commit -m "Release ${NEXT}"
jj bookmark set main -r @-
jj git push --bookmark main
git tag -a "$TAG" -m "Release ${NEXT}"
git push origin "$TAG"

echo "==> released ${NEXT} as ${TAG}"
