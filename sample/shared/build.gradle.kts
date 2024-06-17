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
                // without that Logger type variable declared in this module will have a different type
                // from the Swift side compared to the one declared in Logs module
                export(projects.features.logs)
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            api(projects.features.logs)
            api(projects.features.rum)
        }

        commonMain {
            kotlin.srcDir(generateLibConfigTask.map { it.destinationDir })
        }
    }
}

android {
    namespace = "com.datadog.kmp.sample"
}
