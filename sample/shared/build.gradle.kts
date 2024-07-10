import com.google.gson.JsonParser
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
    alias(libs.plugins.androidLibrary)
    id("datadog-build-config")
    alias(libs.plugins.dependencyLicense)
}

val generateLibConfigTask = tasks.register("generateLibConfig", Sync::class) {

    val configFile = File(Paths.get(rootDir.absolutePath, "config", "us1.json").toUri())
    val (clientToken, rumApplicationId) = if (configFile.exists()) {
        configFile.readText().let {
            val configJson = JsonParser.parseString(it).asJsonObject
            configJson.get("token").asString to configJson.get("rumApplicationId").asString
        }
    } else {
        logger.warn("Credentials config file is not found, using empty values")
        "" to ""
    }

    from(
        resources.text.fromString(
            """
        |package com.datadog.kmp.sample
        |
        |object LibraryConfig {
        |  const val DD_APPLICATION_ID = "$rumApplicationId"
        |  const val DD_CLIENT_TOKEN = "$clientToken"
        |}
        |
            """.trimMargin()
        )
    ) {
        rename { "LibraryConfig.kt" }
        into(Paths.get("com", "datadog", "kmp", "sample").pathString)
    }

    val generatedDirectory = layout.buildDirectory.dir(Paths.get("generated", "datadog").pathString)
    into(generatedDirectory)
}

kotlin {
    targets.all {
        if (this is KotlinNativeTarget) {
            binaries.framework {
                baseName = "sharedLib"
                isStatic = true

                // uncomment this block and comment out block below to check published artifacts fetching
                // export("${ProjectConfig.GROUP_ID}:dd-sdk-kotlin-multiplatform-logs:${ProjectConfig.VERSION.name}")

                // without that Logger type variable declared in this module will have a different type
                // from the Swift side compared to the one declared in Logs module
                export(projects.features.logs)
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            // uncomment this block and comment out block below to check published artifacts fetching
            // val version = ProjectConfig.VERSION.name
            // implementation("${ProjectConfig.GROUP_ID}:dd-sdk-kotlin-multiplatform-core:$version")
            // api("${ProjectConfig.GROUP_ID}:dd-sdk-kotlin-multiplatform-logs:$version")
            // api("${ProjectConfig.GROUP_ID}:dd-sdk-kotlin-multiplatform-rum:$version")
            // api("${ProjectConfig.GROUP_ID}:dd-sdk-kotlin-multiplatform-webview:$version")

            implementation(projects.core)
            api(projects.features.logs)
            api(projects.features.rum)
            api(projects.features.webview)
            api(projects.integrations.ktor)

            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonMain {
            kotlin.srcDir(generateLibConfigTask.map { it.destinationDir })
        }
    }
}

android {
    namespace = "com.datadog.kmp.sample"
}
