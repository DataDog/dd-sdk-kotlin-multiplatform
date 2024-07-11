/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import com.datadog.build.ProjectConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    id("datadog-build-config")
    alias(libs.plugins.dependencyLicense)
    id("api-surface")
    id("transitive-dependencies")
// TODO RUM-5099 Update Mokkery to the version compatible with Kotlin 2.0.20+
//    alias(libs.plugins.mokkery)

    // publishing
    `maven-publish`
    signing
}

kotlin {

    cocoapods {
        // cannot use noPodSpec, because of https://youtrack.jetbrains.com/issue/KT-63331
        // so what is below for podspec description is just a fake thing to make tooling happy
        version = ProjectConfig.VERSION.name
        // need to build with XCode 15
        ios.deploymentTarget = "12.0"
        name = "DatadogKMPKtor"
        summary = "Official Datadog SDK Ktor integration for iOS."

        framework {
            baseName = "DatadogKMPKtor"
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
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            api(projects.features.rum)
            api(libs.ktor.client.core)
            api(libs.uuid)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            // TODO RUM-5099 Update Mokkery to the version compatible with Kotlin 2.0.20+
            implementation("dev.mokkery:mokkery-runtime:${libs.versions.mokkery.get()}")
        }
    }
}

android {
    namespace = "com.datadog.kmp.ktor"
}

// TODO RUM-5099 Update Mokkery to the version compatible with Kotlin 2.0.20+
// mokkery {
//    defaultMockMode = MockMode.autofill
//    ignoreFinalMembers = true
// }
