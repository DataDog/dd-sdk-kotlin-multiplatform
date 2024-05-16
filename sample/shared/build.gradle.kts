import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("datadog-build-config")
    alias(libs.plugins.dependencyLicense)
}

kotlin {
    targets.all {
        if (this is KotlinNativeTarget) {
            binaries.framework {
                baseName = "sharedLib"
                isStatic = true
                // without that Logger type variable declared in this module will have a different type
                // from the Swift side compared to the one declared in Logs module
                export(projects.features.ddSdkKotlinMultiplatformLogs)
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.ddSdkKotlinMultiplatformCore)
            api(projects.features.ddSdkKotlinMultiplatformLogs)
        }
    }
}

android {
    namespace = "com.datadog.kmp.sample"
}
