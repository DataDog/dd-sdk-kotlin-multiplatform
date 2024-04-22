#!/bin/bash

#
# Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
# This product includes software developed at Datadog (https://www.datadoghq.com/).
# Copyright 2016-Present Datadog, Inc.
#

if [ ! -f "settings.gradle.kts" ]; then
	echo "\`check-license.sh\` must be run in repository root folder: \`./tools/license/check-license.sh\`"; exit 1
fi

IFS=$'\n'

# Lists all files requiring the license header.
function files {
	# Exclude all auto-generated and 3rd party files.
	find -E . \
		-iregex '.*\.(swift|h|m|py)$' \
		-type f \( ! -name "Package.swift" \) \
		-not -path "*/.build/*" \
		-not -path "*/build/*" \
		-not -path "*Pods*" \
		-not -path "*Carthage/Build/*" \
		-not -path "*Carthage/Checkouts/*" \
		-not -name "Versioning.swift" \
		-not -name "__init__.py"
}

FILES_WITH_MISSING_LICENSE=""

for file in $(files); do
	if ! grep -q "Apache License Version 2.0" "$file"; then
		FILES_WITH_MISSING_LICENSE="${FILES_WITH_MISSING_LICENSE}\n${file}"
	fi
done

if [ -z "$FILES_WITH_MISSING_LICENSE" ]; then
	echo "✅ All files include the license header"
	exit 0
else
	echo -e "🔥 Missing the license header in files: $FILES_WITH_MISSING_LICENSE"
	exit 1
fi
