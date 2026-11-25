#!/bin/bash

#
# Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
# This product includes software developed at Datadog (https://www.datadoghq.com/).
# Copyright 2016-Present Datadog, Inc.
#

# Bumps the native Datadog SDK versions: `datadog-android` / `datadog-ios` in gradle/libs.versions.toml and,
# for iOS, the dd-sdk-ios package `exactVersion` of every sample Xcode project. Run from the repository root.
#
# Usage: bump_versions.sh [--android <version>] [--ios <version>]

set -euo pipefail

CATALOG="gradle/libs.versions.toml"
android=""
ios=""

while [ $# -gt 0 ]; do
    case "$1" in
        --android) android="$2"; shift 2 ;;
        --ios) ios="$2"; shift 2 ;;
        *) echo "Usage: $0 [--android <version>] [--ios <version>]" >&2; exit 1 ;;
    esac
done

if [ -z "$android" ] && [ -z "$ios" ]; then
    echo "Usage: $0 [--android <version>] [--ios <version>]" >&2
    exit 1
fi
if [ ! -f "$CATALOG" ]; then
    echo "Run from the dd-sdk-kotlin-multiplatform repository root." >&2
    exit 1
fi

if [ -n "$android" ]; then
    sed -i '' -E "s/^datadog-android = \"[^\"]+\"/datadog-android = \"$android\"/" "$CATALOG"
fi

if [ -n "$ios" ]; then
    sed -i '' -E "s/^datadog-ios = \"[^\"]+\"/datadog-ios = \"$ios\"/" "$CATALOG"
    # The `version = ` line follows the `repositoryURL` of the dd-sdk-ios package reference.
    find sample -name project.pbxproj -path '*.xcodeproj/*' -print0 | while IFS= read -r -d '' project; do
        if grep -q 'repositoryURL = "https://github.com/DataDog/dd-sdk-ios"' "$project"; then
            sed -i '' -E '/repositoryURL = "https:\/\/github.com\/DataDog\/dd-sdk-ios"/,/};/ s/(version = )[^;]+;/\1'"$ios"';/' "$project"
            echo "Updated $project"
        fi
    done
fi

grep -E '^datadog-(android|ios) = ' "$CATALOG"
grep -rn -A4 'repositoryURL = "https://github.com/DataDog/dd-sdk-ios"' --include=project.pbxproj sample | grep 'version = ' || true
