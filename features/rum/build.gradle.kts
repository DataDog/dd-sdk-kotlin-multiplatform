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
        noPodspec()

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

mokkery {
    defaultMockMode = MockMode.autofill
    ignoreFinalMembers = true
}

datadogBuildConfig {
    pomDescription = "The RUM feature to use with the Datadog monitoring library for Kotlin Multiplatform."
}

@Suppress("PropertyName", "VariableNaming")
val VIEW_EVENT_MODEL_NAME = "ViewEvent"

@Suppress("PropertyName", "VariableNaming")
val ACTION_EVENT_MODEL_NAME = "ActionEvent"

@Suppress("PropertyName", "VariableNaming")
val ERROR_EVENT_MODEL_NAME = "ErrorEvent"

@Suppress("PropertyName", "VariableNaming")
val RESOURCE_EVENT_MODEL_NAME = "ResourceEvent"

@Suppress("PropertyName", "VariableNaming")
val LONG_TASK_EVENT_MODEL_NAME = "LongTaskEvent"

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
            "action-schema.json" to ACTION_EVENT_MODEL_NAME,
            "error-schema.json" to ERROR_EVENT_MODEL_NAME,
            "resource-schema.json" to RESOURCE_EVENT_MODEL_NAME,
            "view-schema.json" to VIEW_EVENT_MODEL_NAME,
            "long_task-schema.json" to LONG_TASK_EVENT_MODEL_NAME
        )

        @Suppress("StringLiteralDuplication")
        val defaultEnumValues = mapOf(
            "$VIEW_EVENT_MODEL_NAME.${VIEW_EVENT_MODEL_NAME}SessionType" to "USER",
            "$VIEW_EVENT_MODEL_NAME.Status" to "CONNECTED",
            "$VIEW_EVENT_MODEL_NAME.EffectiveType" to "`4G`",
            "$VIEW_EVENT_MODEL_NAME.DeviceType" to "OTHER",
            "$VIEW_EVENT_MODEL_NAME.ReplayLevel" to "MASK_USER_INPUT",
            "$VIEW_EVENT_MODEL_NAME.Plan" to "PLAN_1",
            "$VIEW_EVENT_MODEL_NAME.SessionPrecondition" to "USER_APP_LAUNCH",
            "$VIEW_EVENT_MODEL_NAME.State" to "ACTIVE",
            "$ACTION_EVENT_MODEL_NAME.${ACTION_EVENT_MODEL_NAME}ActionType" to "TAP",
            "$RESOURCE_EVENT_MODEL_NAME.ResourceType" to "OTHER",
            "$RESOURCE_EVENT_MODEL_NAME.OperationType" to "QUERY",
            "$RESOURCE_EVENT_MODEL_NAME.Method" to "GET",
            "$RESOURCE_EVENT_MODEL_NAME.ProviderType" to "OTHER",
            "$ERROR_EVENT_MODEL_NAME.ErrorSource" to "CUSTOM",
            "$ERROR_EVENT_MODEL_NAME.Category" to "EXCEPTION",
            "$ERROR_EVENT_MODEL_NAME.Handling" to "UNHANDLED"
        )

        val commonizeDefaultEnumValues: (Map<String, String>) -> Map<String, String> = {
            it.toMutableMap()
                .apply {
                    val add = entries.flatMap {
                        listOf(
                            it.key.replace(VIEW_EVENT_MODEL_NAME, ACTION_EVENT_MODEL_NAME) to it.value,
                            it.key.replace(VIEW_EVENT_MODEL_NAME, RESOURCE_EVENT_MODEL_NAME) to it.value,
                            it.key.replace(VIEW_EVENT_MODEL_NAME, ERROR_EVENT_MODEL_NAME) to it.value,
                            it.key.replace(VIEW_EVENT_MODEL_NAME, LONG_TASK_EVENT_MODEL_NAME) to it.value,
                            it.key.replace(RESOURCE_EVENT_MODEL_NAME, ERROR_EVENT_MODEL_NAME) to it.value
                        )
                    }
                    putAll(add)
                }
        }

        // NB: model mapping is generated only for the native -> common conversion.
        // Common -> native model mapping generation is not implemented, because of the very few mutable fields,
        // it is faster to write things manually. Once schemas updates, check for the new readOnly:false properties
        // and additionalProperties (they are mutable by default) and update the mapping.

        androidModelsMappingGeneration {
            enabled = true
            androidModelsPackageName = "com.datadog.android.rum.model"
            defaultCommonEnumValues = commonizeDefaultEnumValues(
                defaultEnumValues + mapOf(
                    "$VIEW_EVENT_MODEL_NAME.${VIEW_EVENT_MODEL_NAME}Source" to "ANDROID",
                    "$VIEW_EVENT_MODEL_NAME.LoadingType" to "ACTIVITY_DISPLAY",
                    "$ERROR_EVENT_MODEL_NAME.SourceType" to "ANDROID"
                )
            )
        }

        iosModelsMappingGeneration {
            enabled = true
            iosModelsPackageName = "cocoapods.DatadogObjc"
            iosModelsClassNamePrefix = "DDRUM"
            typeNameRemapping = mapOf(
                "Connectivity" to "RUMConnectivity",
                "USR" to "RUMUser",
                "Method" to "RUMMethod",
                "Context" to "RUMEventAttributes",
                "CiTest" to "RUMCITest",
                "SessionType" to "RUMSessionType",
                "Synthetics" to "RUMSyntheticsTest",
                "Device" to "RUMDevice",
                "OS" to "RUMOperatingSystem",
                "SessionPrecondition" to "RUMSessionPrecondition"
            )
            defaultCommonEnumValues = commonizeDefaultEnumValues(
                defaultEnumValues + mapOf(
                    "$VIEW_EVENT_MODEL_NAME.${VIEW_EVENT_MODEL_NAME}Source" to "IOS",
                    "$VIEW_EVENT_MODEL_NAME.LoadingType" to "VIEW_CONTROLLER_DISPLAY",
                    "$ERROR_EVENT_MODEL_NAME.SourceType" to "IOS"
                )
            )
        }
    }
}
