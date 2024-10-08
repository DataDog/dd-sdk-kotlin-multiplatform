import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
        mavenContent {
            includeGroupByRegex("com\\.github\\..*")
        }
    }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.android.tools)
    compileOnly(libs.kotlin.gradle.plugin)
    implementation(libs.kotlinGrammarParser)
    implementation(libs.kotlinAntlrRuntime)
    implementation(libs.kotlinPoet)
    implementation(libs.gson)

    testImplementation(libs.bundles.jUnit5)
    testImplementation(libs.bundles.jvmTestTools)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

gradlePlugin {
    plugins {
        register("DatadogProjectConfigurationPlugin") {
            id = "datadog-build-config"
            implementationClass = "com.datadog.build.plugin.config.DatadogProjectConfigurationPlugin"
        }
        register("ApiSurfacePlugin") {
            id = "api-surface"
            implementationClass = "com.datadog.build.plugin.apisurface.ApiSurfacePlugin"
        }
        register("TransitiveDependenciesPlugin") {
            id = "transitive-dependencies"
            implementationClass = "com.datadog.build.plugin.transdeps.TransitiveDependenciesPlugin"
        }
        register("GenerateJsonSchemaPlugin") {
            id = "json-schema-generator"
            implementationClass = "com.datadog.build.plugin.jsonschema.GenerateJsonSchemaPlugin"
        }
    }
}
