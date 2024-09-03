/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import com.datadog.build.ProjectConfig
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

    afterEvaluate {
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

        androidModelsMappingGeneration {
            enabled = true
            androidModelsPackageName = "com.datadog.android.rum.model"
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
        }
    }
}
