/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    id("datadog-build-config")
    alias(libs.plugins.dependencyLicense)
    id("api-surface")
    id("transitive-dependencies")

    // publishing
    `maven-publish`
    signing
}

kotlin {

    cocoapods {
        // need to build with XCode 15
        ios.deploymentTarget = "12.0"
        noPodspec()

        framework {
            baseName = "DatadogKMPSessionReplay"
        }

        // need to link it only for the tests so far (maybe this will change
        // later with SDK setup changes)
        pod("DatadogObjc") {
            linkOnly = true
            version = libs.versions.datadog.ios.get()
        }
        pod("DatadogCrashReporting") {
            linkOnly = true
            version = libs.versions.datadog.ios.get()
        }
        pod("DatadogSessionReplay") {
            version = libs.versions.datadog.ios.get()
            extraOpts += listOf(
                // proposed by KMP because of the @import usage in the binary
                "-compiler-option",
                "-fmodules"
            )
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.datadog.android.sessionReplay)
        }
        commonMain.dependencies {
            api(projects.core)
        }
    }
}

android {
    namespace = "com.datadog.kmp.sessionreplay"
}

datadogBuildConfig {
    pomDescription = "The Session Replay feature to use with the Datadog monitoring library for Kotlin Multiplatform."
}
