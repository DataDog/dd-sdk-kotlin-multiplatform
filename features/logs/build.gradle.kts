/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import com.datadog.build.plugin.jsonschema.SchemaLocation
import dev.mokkery.MockMode
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests

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

    targets.withType<KotlinNativeTargetWithSimulatorTests> {
        compilations
            .getByName("test")
            .compileTaskProvider {
                compilerOptions {
                    freeCompilerArgs.addAll(
                        listOf(
                            "-linker-options",
                            // TODO RUM-6046 Name of CrashReporter framework is not passed, so have
                            //  to pass it explicitly, otherwise konanc invocation with linking (ld) fails
                            //  to locate framework for PLCrashReporter pod. Kotlin Compiler bug?
                            "-framework CrashReporter " +
                                // TODO RUM-6047 Kotlin Compiler cannot locate these during the linking
                                //  done via pods integration
                                "-U __swift_FORCE_LOAD_\$_swiftCompatibility56 " +
                                "-U __swift_FORCE_LOAD_\$_swiftCompatibilityConcurrency"
                        )
                    )
                }
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

        androidModelsMappingGeneration {
            enabled = true
            androidModelsPackageName = "com.datadog.android.log.model"
            defaultCommonEnumValues = mapOf("LogEvent.Status" to "INFO")
        }
    }
}
