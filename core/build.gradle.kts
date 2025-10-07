import com.datadog.build.ProjectConfig
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.nio.file.Paths
import kotlin.io.path.pathString

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

    // publishing
    `maven-publish`
    signing
}

val generateLibConfigTask = tasks.register("generateLibConfig", Sync::class) {
    from(
        resources.text.fromString(
            """
        |package com.datadog.kmp.internal
        |
        |internal object LibraryConfig {
        |  const val SDK_VERSION = "${ProjectConfig.VERSION.name}"
        |}
        |
            """.trimMargin()
        )
    ) {
        rename { "LibraryConfig.kt" }
        into(Paths.get("com", "datadog", "kmp", "internal").pathString)
    }

    val generatedDirectory = layout.buildDirectory.dir(Paths.get("generated", "datadog").pathString)
    into(generatedDirectory)
}

kotlin {

    cocoapods {
        // need to build with XCode 15
        ios.deploymentTarget = "12.0"
        tvos.deploymentTarget = "12.0"
        noPodspec()

        framework {
            baseName = "DatadogKMPCore"
        }

        val compilerOptionFlag = "-compiler-option"
        val modulesFlag = "-fmodules"
        pod("DatadogCore") {
            extraOpts += listOf(
                // proposed by KMP because of the @import usage in the binary
                compilerOptionFlag,
                modulesFlag
            )
            version = libs.versions.datadog.ios.get()
        }
        // TODO RUM-11618 DatadogInternal cannot be used
//        pod("DatadogInternal") {
//            extraOpts += listOf(
//                // proposed by KMP because of the @import usage in the binary
//                compilerOptionFlag,
//                modulesFlag
//            )
//            version = libs.versions.datadog.ios.get()
//        }
        pod("DatadogCrashReporting") {
            extraOpts += listOf(
                // proposed by KMP because of the @import usage in the binary
                compilerOptionFlag,
                modulesFlag
            )
            version = libs.versions.datadog.ios.get()
        }
    }

    targets.all {
        if (this is KotlinNativeTarget && konanTarget.family.isAppleFamily) {
            compilations.getByName("main") {
                cinterops.create("DDBinaryImages")
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.datadog.android.core)
            // Android SDK will bring it
            compileOnly(libs.okHttp)
        }
        androidUnitTest.dependencies {
            implementation(libs.bundles.jUnit5)
            implementation(libs.bundles.jvmTestTools)
            compileOnly(libs.okHttp)
        }
        commonMain.dependencies {
            // put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        appleTest.dependencies {
            implementation(projects.tools.unit)
        }
        commonMain {
            kotlin.srcDir(generateLibConfigTask.map { it.destinationDir })
        }
    }
}

android {
    namespace = "com.datadog.kmp"
}

datadogBuildConfig {
    pomDescription = "The Core module of Datadog monitoring library for Kotlin Multiplatform."
}
