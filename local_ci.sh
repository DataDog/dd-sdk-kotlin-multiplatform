#!/usr/bin/env sh

local_ci_usage="Usage: local_ci.sh [-s|--setup] [-n|--clean] [-a|--analysis] [-c|--compile] [-t|--test] [-h|--help]"

KTLINT_VERSION="1.5.0"
DETEKT_VERSION="1.23.8"

TOOLING_DIR="$(cd "$(dirname "$0")" && pwd)/.static-checks-tooling"
KTLINT="$TOOLING_DIR/ktlint-$KTLINT_VERSION"
DETEKT="$TOOLING_DIR/detekt-cli-$DETEKT_VERSION/bin/detekt-cli"

SETUP=0
CLEANUP=0
ANALYSIS=0
COMPILE=0
TEST=0

export CI=true

while [[ $# -gt 0 ]]; do
  case $1 in
  -s | --setup)
    SETUP=1
    shift
    ;;
  -n | --clean)
    CLEANUP=1
    shift
    ;;
  -a | --analysis)
    ANALYSIS=1
    shift
    ;;
  -c | --compile)
    COMPILE=1
    shift
    ;;
  -t | --test)
    TEST=1
    shift
    ;;
  -h | --help)
    echo $local_ci_usage
    shift
    ;;
  *)
    echo "unknown arg: $1"
    echo $local_ci_usage
    exit 1
    ;;
  esac
done

# exit on errors
set -e

# Syncs detekt-common.yml and detekt-public-api.yml from dd-source into config/ at the
# revision pinned by .gitlab-ci.yml. The dd-source repo is restored
# to its prior state afterwards so switching branches there can't affect our checks.
sync_detekt_configs() {
  local config_dir="config"
  local pipeline_file=".gitlab-ci.yml"
  local stamp_file="$config_dir/detekt_dd-source_config.stamp"
  local detekt_common_config="$config_dir/detekt-common.yml"
  local detekt_public_api_config="$config_dir/detekt-public-api.yml"

  mkdir -p "$config_dir"

  local version
  version=$(grep -oE 'gitlab-templates\.ddbuild\.io/mobile/v[0-9]+-[0-9a-f]+/static-analysis\.yml' "$pipeline_file" \
    | head -1 \
    | sed -E 's|.*/mobile/(v[0-9]+-[0-9a-f]+)/.*|\1|')

  if [ -z "$version" ]; then
    echo "  Could not extract dd-source detekt template version from $pipeline_file"
    exit 1
  fi

  # Template tag format: vXXXX-${CI_COMMIT_SHA:0:8}
  local sha="${version##*-}"

  local current=""
  if [ -f "$stamp_file" ]; then
    current=$(cat "$stamp_file")
  fi

  if [ "$current" = "$version" ] && [ -f "$detekt_common_config" ] && [ -f "$detekt_public_api_config" ]; then
    echo "  Detekt configs already at $version"
    return 0
  fi

  echo "  Detekt configs out of date (have '${current:-none}', want '$version'); syncing from dd-source"

  if [ -z "$DD_SOURCE" ]; then
    echo "  DD_SOURCE not set. Please set it to your local dd-source checkout."
    echo "  E.g.: export DD_SOURCE=/Volumes/Dev/ci/dd-source"
    exit 1
  fi
  if [ ! -d "$DD_SOURCE/.git" ]; then
    echo "  DD_SOURCE ($DD_SOURCE) is not a git repository"
    exit 1
  fi

  local sdk_dir
  sdk_dir=$(pwd)

  (
    cd "$DD_SOURCE"

    orig_ref=$(git symbolic-ref --short -q HEAD || git rev-parse HEAD)
    stashed=0

    restore_dd_source() {
      git checkout --quiet "$orig_ref" 2>/dev/null || true
      if [ "$stashed" = "1" ]; then
        git stash pop --quiet 2>/dev/null \
          || echo "  Warning: could not restore stash in $DD_SOURCE; check 'git stash list'"
      fi
    }
    trap restore_dd_source EXIT

    if ! git diff --quiet || ! git diff --cached --quiet || [ -n "$(git ls-files --others --exclude-standard)" ]; then
      git stash push -u -m "dd-sdk-android-detekt-sync" > /dev/null
      stashed=1
    fi

    git fetch --quiet origin main
    if ! git checkout --quiet "$sha" 2>/dev/null; then
      echo "  Could not check out $sha in $DD_SOURCE — make sure dd-source main is up to date"
      exit 1
    fi

    cp "domains/mobile/config/android/gitlab/detekt/detekt-common.yml" "$sdk_dir/$detekt_common_config"
    cp "domains/mobile/config/android/gitlab/detekt/detekt-public-api.yml" "$sdk_dir/$detekt_public_api_config"
  )

  echo "$version" > "$stamp_file"
  echo "  Detekt configs synced to $version"
}

if [[ $SETUP == 1 ]]; then
  echo "-- SETUP"

  mkdir -p "$TOOLING_DIR"

  echo "---- Install KtLint"
  if [[ -x "$KTLINT" ]]; then
    echo "  KtLint $KTLINT_VERSION already installed"
  else
    curl -SL "https://github.com/pinterest/ktlint/releases/download/$KTLINT_VERSION/ktlint" -o "$KTLINT"
    chmod a+x "$KTLINT"
    echo "  KtLint $KTLINT_VERSION installed"
  fi

  echo "---- Install Detekt"
  if [[ -x "$DETEKT" ]]; then
    echo "  Detekt $DETEKT_VERSION already installed"
  else
    curl -SL "https://github.com/detekt/detekt/releases/download/v$DETEKT_VERSION/detekt-cli-$DETEKT_VERSION.zip" -o "$TOOLING_DIR/detekt-cli-$DETEKT_VERSION.zip"
    unzip "$TOOLING_DIR/detekt-cli-$DETEKT_VERSION.zip" -d "$TOOLING_DIR"
    chmod a+x "$TOOLING_DIR/detekt-cli-$DETEKT_VERSION/bin/detekt-cli"
    rm -rf "$TOOLING_DIR/detekt-cli-$DETEKT_VERSION.zip"
    echo "  Detekt $DETEKT_VERSION installed"
  fi
fi

if [[ $CLEANUP == 1 ]]; then
  echo "-- CLEANUP"

  echo "---- Clean repository"
  ./gradlew clean
  rm -rf core/build/
  rm -rf features/logs/build/
  rm -rf features/rum/build/
  rm -rf features/session-replay/build/
  rm -rf features/webview/build/
  rm -rf integrations/ktor/build/
  rm -rf integrations/ktor3/build/

  ./gradlew --stop
fi

if [[ $ANALYSIS == 1 ]]; then
  echo "-- STATIC ANALYSIS"

  echo "---- KtLint (changed files only)"
  CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
  if [ "$CURRENT_BRANCH" = "develop" ]; then
    # On develop: check uncommitted + staged changes
    CHANGED_KT_FILES=$(git diff --name-only --diff-filter=d HEAD -- '*.kt' '*.kts' | grep -v 'build/generated/' | grep -v 'build/kspCaches/' || true)
  else
    # On feature branch: check all changes vs develop (committed + uncommitted)
    CHANGED_KT_FILES=$( (git diff --name-only --diff-filter=d develop... -- '*.kt' '*.kts'; git diff --name-only --diff-filter=d HEAD -- '*.kt' '*.kts') | sort -u | grep -v 'build/generated/' | grep -v 'build/kspCaches/' || true)
  fi
  if [ -n "$CHANGED_KT_FILES" ]; then
    echo "$CHANGED_KT_FILES" | xargs "$KTLINT" -F
  else
    echo "  No changed .kt/.kts files, skipping"
  fi

  echo "---- Detekt"
  echo "------ Sync Detekt configs from dd-source"
  sync_detekt_configs

  echo "------ Detekt common rules"
  "$DETEKT" --parallel --config "config/detekt-common.yml"

  echo "------ Detekt public API rules"
  "$DETEKT" --parallel --config "config/detekt-public-api.yml" --excludes "**/model/*.kt,**/build/**"

  echo "---- AndroidLint"
  ./gradlew :lintCheckAll

  echo "---- 3rd Party License"
  ./gradlew checkDependencyLicenses
fi

if [[ $COMPILE == 1 ]]; then
  echo "-- COMPILATION"

  echo "---- Assemble Android debug variant"
  ./gradlew assembleDebug

  echo "---- Assemble Android debug Unit Tests"
  ./gradlew assembleDebugUnitTest

  echo "---- Assemble iOS debug arm64 target"
  ./gradlew linkPodDebugFrameworkIosArm64

  echo "---- Assemble iOS arm64 test binaries"
  ./gradlew iosArm64TestBinaries
fi

if [[ $TEST == 1 ]]; then
  echo "---- Unit tests (Android+JVM)"
  ./gradlew jvmUnitTestAll

  echo "---- Unit tests (iOS)"
  ./gradlew iosUnitTestAll

  echo "---- Unit tests (tvOS)"
  ./gradlew tvosUnitTestAll
fi

unset CI
echo "-- Done ✔︎"
