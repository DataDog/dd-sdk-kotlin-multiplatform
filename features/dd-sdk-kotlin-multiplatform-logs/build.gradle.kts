import com.datadog.build.AndroidConfig

/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    id("datadog-build-config")
    alias(libs.plugins.dependencyLicense)
    id("api-surface")
    id("transitive-dependencies")
}

kotlin {

    cocoapods {
        // cannot use noPodSpec, because of https://youtrack.jetbrains.com/issue/KT-63331
        // so what is below for podspec description is just a fake thing to make tooling happy
        version = AndroidConfig.VERSION.toString()
        // need to build with XCode 15
        ios.deploymentTarget = "12.0"
        name = "DatadogKMPLogs"
        summary = "Official Datadog KMP Logs SDK for iOS."

        framework {
            baseName = "DatadogKMPLogs"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.datadog.android.logs)
        }
        commonMain.dependencies {
            // put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            // need to have it to use DatadogObjC bindings generated in Core module, to avoid generating it here as well;
            // could be in common dependencies as well, but so far we need only iOS part
            implementation(projects.ddSdkKotlinMultiplatformCore)
        }
    }
}

android {
    namespace = "com.datadog.kmp.log"
}
