import dev.mokkery.MockMode

/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("datadog-build-config")
    id("datadog-ios-frameworks")
    alias(libs.plugins.dependencyLicense)
    id("api-surface")
    id("transitive-dependencies")
    alias(libs.plugins.mokkery)

    // publishing
    `maven-publish`
    signing
}

datadogFrameworks {
    includeObjcCategoryLinkerFlag = true
    framework("DatadogSessionReplay") {
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
            // need to be API, because in androidMain we have extension methods which
            // expose native interface as argument
            api(libs.datadog.android.sessionReplay)
        }
        androidUnitTest.dependencies {
            implementation(libs.bundles.jUnit5)
            implementation(libs.bundles.jvmTestTools)
        }
        commonMain.dependencies {
            api(projects.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.tools.unit)
        }
    }
}

android {
    namespace = "com.datadog.kmp.sessionreplay"
}

mokkery {
    defaultMockMode = MockMode.autofill
    ignoreFinalMembers = true
}

datadogBuildConfig {
    pomDescription = "The Session Replay feature to use with the Datadog monitoring library for Kotlin Multiplatform."
}
