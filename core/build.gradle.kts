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
        name = "DatadogKMPCore"
        summary = "Official Datadog KMP SDK for iOS."

        framework {
            baseName = "DatadogKMPCore"
        }

        pod("DatadogObjc") {
            extraOpts += listOf(
                // proposed by KMP because of the @import usage in the binary
                "-compiler-option",
                "-fmodules",
                // see https://youtrack.jetbrains.com/issue/KT-61799
                // TL;DR: Kotlin interop adds "<ClassName>Meta" class for every "<ClassName>" class,
                // so since there is DDRUMErrorEventError, it generates DDRUMErrorEventErrorMeta, but such
                // class is already declared, leading to error: 'DDRUMErrorEventErrorMeta' is going
                // to be declared twice
                "-compiler-option",
                "-DDDRUMErrorEventErrorMeta=DDRUMErrorEventErrorMetaUnavailable"
            )
            version = libs.versions.datadog.ios.get()
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.datadog.android.core)
        }
        commonMain.dependencies {
            // put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.datadog.kmp"
}