/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    // Build
    alias(libs.plugins.kotlinMultiplatform)
    id("datadog-build-config")

    // Analysis tools
    id("ktlint")
    id("detekt-conventions")
    alias(libs.plugins.dependencyLicense)
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.bundles.jUnit5)
            implementation(libs.bundles.jvmTestTools)
        }
    }
}
