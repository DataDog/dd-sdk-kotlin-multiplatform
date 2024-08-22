/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import com.datadog.build.ProjectConfig
import com.datadog.build.plugin.jsonschema.SchemaLocation

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
    id("json-schema-generator")

    // publishing
    `maven-publish`
    signing
}

kotlin {

    cocoapods {
        // cannot use noPodSpec, because of https://youtrack.jetbrains.com/issue/KT-63331
        // so what is below for podspec description is just a fake thing to make tooling happy
        version = ProjectConfig.VERSION.name
        // need to build with XCode 15
        ios.deploymentTarget = "12.0"
        name = "DatadogKMPRUM"
        summary = "Official Datadog KMP RUM SDK for iOS."

        framework {
            baseName = "DatadogKMPRUM"
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
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.datadog.android.rum)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.navigation.runtime.forSdk)
        }
        androidUnitTest.dependencies {
            implementation(libs.bundles.jUnit5)
            implementation(libs.bundles.jvmTestTools)
            implementation(projects.tools.unit)
        }
        commonMain.dependencies {
            api(projects.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            // TODO RUM-5099 Update Mokkery to the version compatible with Kotlin 2.0.20+
            implementation("dev.mokkery:mokkery-runtime:${libs.versions.mokkery.get()}")
        }
        iosTest.dependencies {
            implementation(projects.tools.unit)
        }
    }

    configurations.androidMainImplementation {
        // this is because we have to use FragmentX 1.5.1 (because 1.4.x ships Lint rules which are not
        // compatible with AGP 8.4.+), and it brings these dependencies. We can strip them out, because since Kotlin
        // 1.8 everything is in the main stdlib.
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    }
}

android {
    namespace = "com.datadog.kmp.rum"
}

// TODO RUM-5099 Update Mokkery to the version compatible with Kotlin 2.0.20+
// mokkery {
//    defaultMockMode = MockMode.autofill
//    ignoreFinalMembers = true
// }

datadogBuildConfig {
    pomDescription = "The RUM feature to use with the Datadog monitoring library for Kotlin Multiplatform."
}

jsonSchemaGenerator {
    schema("rum") {
        location = SchemaLocation.Git(
            repo = "https://github.com/DataDog/rum-events-format.git",
            subFolder = "schemas/rum",
            destinationFolder = "src/commonMain/json/rum",
            ref = "master"
        )
        targetPackageName = "com.datadog.kmp.rum.model"
        ignoredFiles = listOf(
            "_common-schema.json",
            "_perf-metric-schema.json",
            "_action-child-schema.json",
            "_view-container-schema.json",
            "vital-schema.json"
        )
        inputNameMapping = mapOf(
            "action-schema.json" to "ActionEvent",
            "error-schema.json" to "ErrorEvent",
            "resource-schema.json" to "ResourceEvent",
            "view-schema.json" to "ViewEvent",
            "long_task-schema.json" to "LongTaskEvent"
        )
    }
}
