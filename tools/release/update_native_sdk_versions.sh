#!/bin/bash

#
# Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
# This product includes software developed at Datadog (https://www.datadoghq.com/).
# Copyright 2016-Present Datadog, Inc.
#

if [ $# -ne 1 ]; then
    echo "Usage: $0 <sdk_version>"
    exit 1
fi

SDK_VERSION=$1 # The new KMM SDK version
LIBS_VERSION_FILE="./gradle/libs.versions.toml"
OUTPUT_FILE="./NATIVE_SDK_VERSIONS.md"

ios_version=$(grep 'datadog-ios =.*".*"' "$LIBS_VERSION_FILE" | sed 's/.*"\(.*\)".*/\1/')
android_version=$(grep 'datadog-android =.*".*"' "$LIBS_VERSION_FILE" | sed 's/.*"\(.*\)".*/\1/')

# Check if NATIVE_SDK_VERSIONS.md exists, create it otherwise
if [ ! -f $OUTPUT_FILE ]; then
    echo "| Kotlin Multiplatform | iOS SDK | Android SDK |" > $OUTPUT_FILE
    echo "|-------------|---------------------|----------------------------|" >> $OUTPUT_FILE
    echo "Creating new $OUTPUT_FILE file with header"
fi

# Add the new version triad to NATIVE_SDK_VERSIONS.md
if [ ! -z "$SDK_VERSION" ] && [ ! -z "$ios_version" ] && [ ! -z "$android_version" ]; then
    new_row="| $SDK_VERSION | $ios_version | $android_version |"
    
    first_version_row=$(sed -n '3p' $OUTPUT_FILE)
    existing_sdk_version=$(echo "$first_version_row" | sed 's/| *\([^ |]*\) .*/\1/')
    
    if [ "$existing_sdk_version" = "$SDK_VERSION" ]; then
        echo "Entry for version $SDK_VERSION already exists in $OUTPUT_FILE"
    else
        sed -i '' "2a\\
$new_row\\
" NATIVE_SDK_VERSIONS.md
        echo "Updated $OUTPUT_FILE with entry for version $SDK_VERSION"
    fi
else
    echo "Error: Missing version information"
    exit 1
fi
