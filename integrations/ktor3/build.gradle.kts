/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import dev.mokkery.MockMode
import java.nio.file.Paths

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("datadog-build-config")
    id("datadog-ios-frameworks")
    alias(libs.plugins.dependencyLicense)
    id("api-surface")
    id("transitive-dependencies")
    alias(libs.plugins.mokkery)

    // publishing
    `maven-publish`
    signing
}

datadogFrameworks {
    framework("DatadogRUM") {
        linkOnly = true
    }
    framework("DatadogCore") {
        linkOnly = true
    }
    framework("DatadogCrashReporting") {
        linkOnly = true
    }
}

kotlin {
    sourceSets {
        commonMain {
            kotlin {
                srcDirs(Paths.get("..", "ktor", "src", "commonMain").toFile())
            }
            dependencies {
                api(projects.core)
                api(projects.features.rum)
                implementation(libs.ktor3.client.core)
                implementation(libs.uuid)
            }
        }
        commonTest {
            kotlin {
                srcDirs(Paths.get("..", "ktor", "src", "commonTest").toFile())
            }
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.ktor3.client.mock)
                implementation(projects.tools.unit)
            }
        }
        appleMain {
            kotlin {
                srcDirs(Paths.get("..", "ktor", "src", "appleMain").toFile())
            }
        }
        androidMain {
            kotlin {
                srcDirs(Paths.get("..", "ktor", "src", "androidMain").toFile())
            }
        }
    }

    android {
        namespace = "com.datadog.kmp.ktor"
    }
}

mokkery {
    defaultMockMode = MockMode.autofill
    ignoreFinalMembers = true
}

datadogBuildConfig {
    pomDescription = "The Ktor 3 integration to use with the Datadog monitoring library for Kotlin Multiplatform."
}
