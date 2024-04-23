/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("unused")
class DatadogProjectConfigurationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("datadogBuildConfig", DatadogBuildConfigExtension::class.java)
        target.pluginManager.withPlugin("com.android.application") {
            target.logger.info("Found Android Application Plugin, applying configuration")
            target.applyKotlinConfig(extension)
            target.applyApplicationAndroidConfig(extension)
        }

        target.pluginManager.withPlugin("com.android.library") {
            target.logger.info("Found Android Library Plugin, applying configuration")
            target.applyKotlinConfig(extension)
            target.applyLibraryAndroidConfig(extension)
        }

        target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            target.logger.info("Found Kotlin Multiplatform Plugin, applying configuration")
            target.applyKotlinMultiplatformConfig(extension)
        }
    }
}

// region Kotlin

private fun Project.applyKotlinConfig(configExtension: DatadogBuildConfigExtension) {
    taskConfig<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(configExtension.jvmTargetOrDefault)
            allWarningsAsErrors.set(true)
            apiVersion.set(configExtension.kotlinVersionOrDefault)
            languageVersion.set(configExtension.kotlinVersionOrDefault)
        }
    }
}

private fun Project.applyKotlinMultiplatformConfig(extension: DatadogBuildConfigExtension) {
    extensions.getByType(KotlinMultiplatformExtension::class.java)
        .apply {
            androidTarget {
                compilations.all {
                    kotlinOptions {
                        jvmTarget = extension.jvmTargetOrDefault.target
                    }
                }
            }
            targets.all {
                compilations.all {
                    kotlinOptions {
                        apiVersion = extension.kotlinVersionOrDefault.version
                        languageVersion = extension.kotlinVersionOrDefault.version
                        allWarningsAsErrors = true
                    }
                }
            }
        }
}

// endregion

// region Android

private fun Project.applyApplicationAndroidConfig(configExtension: DatadogBuildConfigExtension) {
    extensions.getByType(BaseAppModuleExtension::class.java)
        .apply {
            val javaVersion = configExtension.jvmTargetOrDefault.toJavaVersion()
            compileOptions {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
            compileSdk = AndroidConfig.COMPILE_SDK
            buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION

            defaultConfig {
                minSdk = AndroidConfig.MIN_SDK
                targetSdk = AndroidConfig.COMPILE_SDK
                versionCode = AndroidConfig.VERSION.code
                versionName = AndroidConfig.VERSION.name
            }

            sourceSets.all {
                java.srcDir("src/$name/kotlin")
            }

            @Suppress("UnstableApiUsage")
            testOptions {
                unitTests.isReturnDefaultValues = true
            }

            lintConfigure()
            packagingConfigure()
        }
}

private fun Project.applyLibraryAndroidConfig(configExtension: DatadogBuildConfigExtension) {
    extensions.getByType(LibraryExtension::class.java)
        .apply {
            val javaVersion = configExtension.jvmTargetOrDefault.toJavaVersion()
            compileOptions {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
            compileSdk = AndroidConfig.COMPILE_SDK
            buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION

            defaultConfig {
                minSdk = AndroidConfig.MIN_SDK
            }

            sourceSets.all {
                java.srcDir("src/$name/kotlin")
            }

            @Suppress("UnstableApiUsage")
            testOptions {
                unitTests.isReturnDefaultValues = true
            }

            lintConfigure()
            packagingConfigure()
        }
}

private fun CommonExtension<*, *, *, *, *, *>.lintConfigure() {
    lint {
        warningsAsErrors = true
        abortOnError = true
        checkReleaseBuilds = false
        checkGeneratedSources = true
        ignoreTestSources = true
    }
}

private fun CommonExtension<*, *, *, *, *, *>.packagingConfigure() {
    packaging {
        resources {
            excludes += listOf(
                "META-INF/jvm.kotlin_module",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/{AL2.0,LGPL2.1}"
            )
        }
    }
}

// endregion

inline fun <reified T : Task> Project.taskConfig(
    crossinline configure: T.() -> Unit
) {
    afterEvaluate {
        tasks.withType(T::class.java) { configure() }
    }
}

private fun JvmTarget.toJavaVersion(): JavaVersion {
    // list only LTS releases
    return when (this) {
        JvmTarget.JVM_1_8 -> JavaVersion.VERSION_1_8
        JvmTarget.JVM_11 -> JavaVersion.VERSION_11
        JvmTarget.JVM_17 -> JavaVersion.VERSION_17
        JvmTarget.JVM_21 -> JavaVersion.VERSION_21
        else -> throw IllegalArgumentException("Unknown JvmTarget=${this.name}")
    }
}