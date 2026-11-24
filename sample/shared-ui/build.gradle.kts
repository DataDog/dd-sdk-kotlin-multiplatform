/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/*
 * Compose Multiplatform UI of the sample, used by the Android app (:sample:androidApp) and by the iOS Compose
 * Multiplatform app (sample/iosComposeMultiplatformApp). Non-UI code (SDK setup, logs, RUM, network calls) comes
 * from :sample:shared. tvOS is not supported by Compose Multiplatform, and the SwiftUI app (sample/appleApp) doesn't
 * link this module, so it stays free of Compose.
 */
plugins {
    // Build
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeMultiplatform)
    id("datadog-build-config")

    // Analysis tools
    id("ktlint")
    id("detekt-conventions")
    alias(libs.plugins.dependencyLicense)
}

kotlin {
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "composeSampleLib"
            isStatic = true

            // make the non-UI sample code and Logger type available from Swift
            export(projects.sample.shared)
            export(projects.features.logs)
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.sample.shared)
            api(projects.features.logs)
            implementation(projects.features.rum)

            api(libs.cmp.runtime)
            api(libs.cmp.foundation)
            api(libs.cmp.ui)
            implementation(libs.cmp.ui.tooling.preview)
            implementation(libs.cmp.material3)
            implementation(libs.cmp.navigation.compose)
            implementation(libs.cmp.lifecycle.runtime.compose)
        }

        androidMain.dependencies {
            implementation(libs.datadog.android.compose)
            // has to be here, because compose module above has some classes from Android RUM in public API and
            // compose module has RUM as "implementation", not "api". This is mentioned in README
            implementation(libs.datadog.android.rum)
            implementation(libs.datadog.android.sessionReplayCompose)
        }
    }

    android {
        namespace = "com.datadog.kmp.sample.cmp"
    }
}

dependencies {
    // renders @Preview functions in Android Studio (Android only, not packaged into the library)
    androidRuntimeClasspath(libs.cmp.ui.tooling)
}
