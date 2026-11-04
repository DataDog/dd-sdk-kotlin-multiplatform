/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import dev.mokkery.MockMode

plugins {
    // Build
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("datadog-build-config")
    id("datadog-ios-frameworks")

    // Publish
    `maven-publish`
    signing

    // Analysis tools
    id("ktlint")
    id("detekt-conventions")
    alias(libs.plugins.dependencyLicense)

    // Tests
    alias(libs.plugins.mokkery)

    // Internal Generation
    id("api-surface")
    id("transitive-dependencies")
}

datadogFrameworks {
    framework("DatadogRUM") {
        linkOnly = true
    }
    framework("DatadogCore") {
        linkOnly = true
    }
    framework("DatadogCrashReporting") {
        linkOnly = true
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            api(projects.features.rum)
            implementation(libs.ktor2.client.core)
            implementation(libs.uuid)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor2.client.mock)
            implementation(projects.tools.unit)
        }
    }

    android {
        namespace = "com.datadog.kmp.ktor"
    }
}

mokkery {
    defaultMockMode = MockMode.autofill
    ignoreFinalMembers = true
}

datadogBuildConfig {
    pomDescription = "The Ktor 2 integration to use with the Datadog monitoring library for Kotlin Multiplatform."
}
