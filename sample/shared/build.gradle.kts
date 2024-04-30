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
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.ddSdkKotlinMultiplatformCore)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.datadog.kmp.sample"
}
