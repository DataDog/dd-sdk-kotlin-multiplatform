/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    // Build
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.builtInKotlin)
    alias(libs.plugins.compose.compiler)
    id("datadog-build-config")

    // Analysis tools
    // ktlint must be applied after built-in Kotlin, otherwise it doesn't pick up the Kotlin source sets
    id("ktlint")
    id("detekt-conventions")
    alias(libs.plugins.dependencyLicense)
}

android {
    namespace = "com.datadog.kmp.android.sample"
    defaultConfig {
        applicationId = "com.datadog.kmp.android.sample"
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    lint {
        disable += "MonochromeLauncherIcon"
    }
}

dependencies {
    // Compose (and its version) comes from the shared Compose Multiplatform UI module, which on Android is backed
    // by the AndroidX Compose artifacts
    implementation(projects.sample.sharedUi)
    implementation(libs.androidx.activity.compose)
    // used by the Compose tooling of Android Studio (Layout Inspector) at runtime
    debugImplementation(libs.cmp.ui.tooling)
}
