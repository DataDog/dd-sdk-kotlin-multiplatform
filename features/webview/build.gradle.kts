/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import com.datadog.build.AndroidConfig
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

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
}

kotlin {

    cocoapods {
        // cannot use noPodSpec, because of https://youtrack.jetbrains.com/issue/KT-63331
        // so what is below for podspec description is just a fake thing to make tooling happy
        version = AndroidConfig.VERSION.toString()
        // need to build with XCode 15
        ios.deploymentTarget = "12.0"
        name = "DatadogKMPWebView"
        summary = "Official Datadog KMP WebView tracking SDK for iOS."

        framework {
            baseName = "DatadogKMPWebView"
            isStatic = true
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
        pod("DatadogWebViewTracking") {
            // TODO RUM-5208 by some reason ootb bindings for DatadogWebViewTracking are not generated correctly, so
            //  we go with a custom header (see custom cinterop below)
            linkOnly = true
            version = libs.versions.datadog.ios.get()
        }
    }

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

// TODO RUM-5099 Update Mokkery to the version compatible with Kotlin 2.0.20+
// mokkery {
//    defaultMockMode = MockMode.autofill
//    ignoreFinalMembers = true
// }