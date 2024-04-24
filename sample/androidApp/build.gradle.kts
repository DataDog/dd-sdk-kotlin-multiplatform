/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("datadog-build-config")
}

android {
    namespace = "com.datadog.kmp.android.sample"
    defaultConfig {
        applicationId = "com.datadog.kmp.android.sample"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
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
    implementation(projects.sample.shared)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.compose.ui.tooling)
}
