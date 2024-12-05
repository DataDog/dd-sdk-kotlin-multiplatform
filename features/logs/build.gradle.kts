/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import com.datadog.build.plugin.jsonschema.SchemaLocation
import dev.mokkery.MockMode

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    id("datadog-build-config")
    alias(libs.plugins.dependencyLicense)
    id("api-surface")
    id("transitive-dependencies")
    alias(libs.plugins.mokkery)
    id("json-schema-generator")

    // publishing
    `maven-publish`
    signing
}

kotlin {

    cocoapods {
        // need to build with XCode 15
        ios.deploymentTarget = "12.0"
        tvos.deploymentTarget = "12.0"
        noPodspec()

        framework {
            baseName = "DatadogKMPLogs"
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
            implementation(libs.datadog.android.logs)
        }
        androidUnitTest.dependencies {
            implementation(libs.bundles.jUnit5)
            implementation(libs.bundles.jvmTestTools)
        }
        commonMain.dependencies {
            api(projects.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.tools.unit)
        }
    }
}

android {
    namespace = "com.datadog.kmp.log"
}

mokkery {
    defaultMockMode = MockMode.autofill
    ignoreFinalMembers = true
}

datadogBuildConfig {
    pomDescription = "The Logs feature to use with the Datadog monitoring library for Kotlin Multiplatform."
}

jsonSchemaGenerator {
    schema("logs") {
        location = SchemaLocation.Local(
            path = "src/commonMain/json/log"
        )
        targetPackageName = "com.datadog.kmp.log.model"

        // NB: model mapping is generated only for the native -> common conversion.
        // Common -> native model mapping generation is not implemented, because of the very few mutable fields,
        // it is faster to write things manually. Once schemas updates, check for the new readOnly:false properties
        // and additionalProperties (they are mutable by default) and update the mapping.

        androidModelsMappingGeneration {
            enabled = true
            androidModelsPackageName = "com.datadog.android.log.model"
            defaultCommonEnumValues = mapOf("LogEvent.Status" to "INFO")
        }
    }
}
