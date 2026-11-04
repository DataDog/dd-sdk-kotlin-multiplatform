#!/usr/bin/env sh

local_ci_usage="Usage: local_ci.sh [-n|--clean] [-a|--analysis] [-c|--compile] [-t|--test] [-h|--help]"

CLEANUP=0
ANALYSIS=0
COMPILE=0
TEST=0

export CI=true

while [[ $# -gt 0 ]]; do
  case $1 in
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
  ./gradlew ktlintFormatAll

  echo "---- Detekt"
  ./gradlew detektAll --continue

  echo "---- AndroidLint"
  ./gradlew lintRelease

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
