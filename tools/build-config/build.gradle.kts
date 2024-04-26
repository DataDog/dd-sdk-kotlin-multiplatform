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
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.android.tools)
    compileOnly(libs.kotlin.gradle.plugin)
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
            implementationClass = "com.datadog.build.DatadogProjectConfigurationPlugin"
        }
    }
}
