#!/usr/bin/env sh

local_ci_usage="Usage: local_ci.sh [-s|--setup] [-n|--clean] [-a|--analysis] [-c|--compile] [-t|--test] [-h|--help]"

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

if [[ $SETUP == 1 ]]; then
  echo "-- SETUP"

  echo "---- Install KtLint"
  if [[ -x "$(command -v ktlint)" ]]; then
      echo "  KtLint already installed; version $(ktlint --version)"
  else
    curl -SLO https://github.com/pinterest/ktlint/releases/download/0.50.0/ktlint && chmod a+x ktlint
    sudo mv ktlint /usr/local/bin/
    echo "  KtLint installed; version $(ktlint --version)"
  fi

  echo "---- Install Detekt"
  if [[ -x "$(command -v detekt)" ]]; then
      echo "  Detekt already installed; version $(detekt --version)"
  else
    brew install detekt
    echo "  Detekt installed; version $(detekt --version)"
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

  echo "---- KtLint"
  ktlint -F "**/*.kt" "**/*.kts" '!**/build/generated/**' '!**/build/kspCaches/**'

  echo "---- Detekt"
  if [ -z $DD_SOURCE ]; then
    echo "Can't run shared Detekt, missing dd_source repository path."
    echo "Please set the path to your local dd_source repository in the DD_SOURCE environment variable."
    echo "E.g.: "
    echo "$ export DD_SOURCE=/Volumes/Dev/ci/dd-source"
    exit 1
  else
    echo "Using Detekt rules from $DD_SOURCE folder"
  fi

  echo "------ Detekt common rules"
  detekt --config "$DD_SOURCE/domains/mobile/config/android/gitlab/detekt/detekt-common.yml"

  # echo "------ Detekt public API rules"
  # detekt --config "$DD_SOURCE/domains/mobile/config/android/gitlab/detekt/detekt-public-api.yml"

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
