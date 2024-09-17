/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.dependencyLicense)
    id("datadog-build-config")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.bundles.jUnit5)
            implementation(libs.bundles.jvmTestTools)
        }
    }
}
