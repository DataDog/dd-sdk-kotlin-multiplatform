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
    framework("DatadogWebViewTracking") {
        linkOnly = false
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
        androidMain.dependencies {
            implementation(libs.datadog.android.webview)
        }
        commonMain.dependencies {
            api(projects.core)
        }
        iosTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.tools.unit)
        }
    }

    android {
        namespace = "com.datadog.kmp.webview"
    }
}

mokkery {
    defaultMockMode = MockMode.autofill
    ignoreFinalMembers = true
}

datadogBuildConfig {
    pomDescription = "The WebView tracking feature to use with the Datadog monitoring library for Kotlin Multiplatform."
}
