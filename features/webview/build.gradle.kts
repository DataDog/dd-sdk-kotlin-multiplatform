/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import dev.mokkery.MockMode
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

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
    framework("DatadogWebViewTracking") {
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
    targets.all {
        if (this is KotlinNativeTarget && konanTarget.family == Family.IOS) {
            compilations.getByName("main") {
                cinterops.create("DatadogWebView") {
                    includeDirs("$projectDir/src/nativeInterop/cinterop/DatadogWebViewTracking")
                }
            }
        }
    }

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
}

android {
    namespace = "com.datadog.kmp.webview"
}

mokkery {
    defaultMockMode = MockMode.autofill
    ignoreFinalMembers = true
}

datadogBuildConfig {
    pomDescription = "The WebView tracking feature to use with the Datadog monitoring library for Kotlin Multiplatform."
}
