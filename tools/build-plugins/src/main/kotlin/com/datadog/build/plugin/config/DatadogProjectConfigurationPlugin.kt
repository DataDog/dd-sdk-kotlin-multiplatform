/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.config

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.datadog.build.ProjectConfig
import com.datadog.build.utils.taskConfig
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.targets.native.KotlinNativeSimulatorTestRun
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class DatadogProjectConfigurationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<DatadogBuildConfigExtension>("datadogBuildConfig")
        target.pluginManager.withPlugin("com.android.application") {
            target.logger.info("Found Android Application Plugin in project ${target.path}, applying configuration")
            target.applyKotlinConfig(extension)
            target.applyApplicationAndroidConfig(extension)
        }

        target.pluginManager.withPlugin("com.android.library") {
            target.logger.info("Found Android Library Plugin in project ${target.path}, applying configuration")
            target.applyKotlinConfig(extension)
            target.applyLibraryAndroidConfig(extension)
        }

        target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            target.logger.info("Found Kotlin Multiplatform Plugin in project ${target.path}, applying configuration")
            target.applyKotlinMultiplatformConfig(extension)
        }

        target.pluginManager.withPlugin("org.gradle.maven-publish") {
            target.logger.info("Found Maven Publish Plugin in project ${target.path}, applying configuration")
            target.applyPublishingConfig(extension)
        }
    }
}

// region Kotlin

private fun Project.applyKotlinConfig(configExtension: DatadogBuildConfigExtension) {
    kotlinExtension.apply {
        sourceSets.all {
            languageSettings {
                languageVersion = configExtension.kotlinVersionOrDefault.version
                apiVersion = configExtension.kotlinVersionOrDefault.version
            }
        }
    }
    taskConfig<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(configExtension.jvmTargetOrDefault)
            // there are few warnings coming from the fact that the JetBrains Compose Compiler is the new one, but AGP
            // is still using old values for the configuration
            // w: intrinsicRemember is deprecated. Use
            // plugin:androidx.compose.compiler.plugins.kotlin:featureFlag=IntrinsicRemember instead
            // w: nonSkippingGroupOptimization is deprecated. Use
            // plugin:androidx.compose.compiler.plugins.kotlin:featureFlag=OptimizeNonSkippingGroups instead
            // w: experimentalStrongSkipping is deprecated. Use
            // plugin:androidx.compose.compiler.plugins.kotlin:featureFlag=StrongSkipping instead
            allWarningsAsErrors.set(project.name != "androidApp")
        }
    }
    taskConfig<Test> {
        useJUnitPlatform()
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
private fun Project.applyKotlinMultiplatformConfig(configExtension: DatadogBuildConfigExtension) {
    val projectToApply = this
    extensions.getByType<KotlinMultiplatformExtension>()
        .apply {
            if (!projectToApply.displayName.contains("tools")) {
                androidTarget {
                    compilerOptions {
                        jvmTarget.set(configExtension.jvmTargetOrDefault)
                    }
                }
            } else {
                jvm()
            }
            // TODO RUM-4231 Add other Apple targets (tvOS, watchOS, etc.)
            iosX64()
            iosArm64()
            iosSimulatorArm64()

            sourceSets.all {
                if (name.startsWith("ios")) {
                    languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }

            targets.all {
                if (this is KotlinNativeTargetWithTests<*>) {
                    testRuns.all {
                        if (this is KotlinNativeSimulatorTestRun) {
                            // Need to find a way to be more precise, to specify OS runtime version. Should be
                            // aligned with what is in CI file.
                            deviceId = "iPhone 15 Pro Max"
                        }
                    }
                }
                if (this is HasConfigurableKotlinCompilerOptions<*>) {
                    compilerOptions {
                        if (this is KotlinJvmCompilerOptions) {
                            jvmTarget.set(configExtension.jvmTargetOrDefault)
                        }
                        // https://kotlinlang.org/docs/components-stability.html#current-stability-of-kotlin-components
                        // https://youtrack.jetbrains.com/issue/KT-61573
                        // expect/actual classes are in beta since 1.7.20 (and they still are as of 1.9.24), but we
                        // are going to use them anyway
                        freeCompilerArgs.add("-Xexpect-actual-classes")
                        apiVersion.set(configExtension.kotlinVersionOrDefault)
                        languageVersion.set(configExtension.kotlinVersionOrDefault)
                        allWarningsAsErrors.set(true)
                    }
                }
            }
            afterEvaluate {
                // is not taken into account in KMP by some reason if without afterEvaluate
                sourceSets.all {
                    languageSettings {
                        languageVersion = configExtension.kotlinVersionOrDefault.version
                        apiVersion = configExtension.kotlinVersionOrDefault.version
                    }
                }
            }
        }
}

// endregion

// region Android

private fun Project.applyApplicationAndroidConfig(configExtension: DatadogBuildConfigExtension) {
    extensions.getByType<BaseAppModuleExtension>()
        .apply {
            val javaVersion = configExtension.jvmTargetOrDefault.toJavaVersion()
            compileOptions {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
            compileSdk = ProjectConfig.Android.COMPILE_SDK
            buildToolsVersion = ProjectConfig.Android.BUILD_TOOLS_VERSION

            defaultConfig {
                minSdk = ProjectConfig.Android.MIN_SDK
                targetSdk = ProjectConfig.Android.COMPILE_SDK
                versionCode = ProjectConfig.VERSION.code
                versionName = ProjectConfig.VERSION.name
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
    extensions.getByType<LibraryExtension>()
        .apply {
            val javaVersion = configExtension.jvmTargetOrDefault.toJavaVersion()
            compileOptions {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
            compileSdk = ProjectConfig.Android.COMPILE_SDK
            buildToolsVersion = ProjectConfig.Android.BUILD_TOOLS_VERSION

            defaultConfig {
                minSdk = ProjectConfig.Android.MIN_SDK
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
        // GradleDependency check: A newer version of com.foo.bar than x.x.x is available: y.y.y
        disable += "GradleDependency"
        // AndroidGradlePluginVersion: A newer version of com.android.tools.build:gradle than x.x.x is available: y.y.y
        disable += "AndroidGradlePluginVersion"
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

// region Publishing

private fun Project.applyPublishingConfig(buildConfigExtension: DatadogBuildConfigExtension) {
    extensions.getByType<KotlinMultiplatformExtension>()
        .targets
        .withType<KotlinAndroidTarget> {
            publishLibraryVariants("release")
        }

    val publishingExtension = extensions.getByType<PublishingExtension>()
        .apply {
            publications.withType<MavenPublication> {
                groupId = ProjectConfig.GROUP_ID
                version = ProjectConfig.VERSION.name

                // afterEvaluate here is important
                afterEvaluate {
                    artifactId = "dd-sdk-kotlin-multiplatform-$artifactId"
                }

                pom {
                    name.set(artifactId)
                    description.set(
                        buildConfigExtension.pomDescription.map {
                            it.ifEmpty {
                                throw IllegalStateException("Published projects should have a description")
                            }
                        }
                    )
                    url.set("https://github.com/DataDog/dd-sdk-kotlin-multiplatform/")

                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    organization {
                        name.set("Datadog")
                        url.set("https://www.datadoghq.com/")
                    }
                    developers {
                        developer {
                            name.set("Datadog")
                            email.set("info@datadoghq.com")
                            organization.set("Datadog")
                            organizationUrl.set("https://www.datadoghq.com/")
                        }
                    }

                    scm {
                        url.set("https://github.com/DataDog/dd-sdk-kotlin-multiplatform/")
                        connection.set("scm:git:git@github.com:Datadog/dd-sdk-kotlin-multiplatform.git")
                        developerConnection.set("scm:git:git@github.com:Datadog/dd-sdk-kotlin-multiplatform.git")
                    }
                }
            }
        }

    afterEvaluate {
        extensions.getByType<SigningExtension>()
            .apply {
                val privateKey = System.getenv("GPG_PRIVATE_KEY")
                val password = System.getenv("GPG_PASSWORD")
                isRequired = !hasProperty("dd-skip-signing")
                useInMemoryPgpKeys(privateKey, password)
                sign(publishingExtension.publications)
            }
    }
}

// endregion

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
