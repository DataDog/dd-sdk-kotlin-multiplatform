/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import dev.mokkery.MockMode
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    id("datadog-build-config")
    alias(libs.plugins.dependencyLicense)
    id("api-surface")
    id("transitive-dependencies")
    alias(libs.plugins.mokkery)

    // publishing
    `maven-publish`
    signing
}

kotlin {

    cocoapods {
        // need to build with XCode 15
        ios.deploymentTarget = "12.0"
        tvos.deploymentTarget = "12.0"
        noPodspec()

        framework {
            baseName = "DatadogKMPKtor"
        }

        // need to link it only for the tests so far (maybe this will change
        // later with SDK setup changes)
        pod("DatadogRUM") {
            linkOnly = true
            version = libs.versions.datadog.ios.get()
        }
        pod("DatadogCore") {
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
            implementation(libs.ktor2.client.core)
            implementation(libs.uuid)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor2.client.mock)
            implementation(projects.tools.unit)
        }
        appleMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
    }

    // fix for w: KLIB resolver: The same 'unique_name=org.jetbrains.kotlinx:atomicfu' found in more than one library
    // see https://youtrack.jetbrains.com/issue/KT-71206, should be fixed in Kotlin 2.1.0
    targets.withType<KotlinNativeTargetWithSimulatorTests> {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions.allWarningsAsErrors = false
    }
}

android {
    namespace = "com.datadog.kmp.ktor"
}

mokkery {
    defaultMockMode = MockMode.autofill
    ignoreFinalMembers = true
}

datadogBuildConfig {
    pomDescription = "The Ktor 2 integration to use with the Datadog monitoring library for Kotlin Multiplatform."
}
