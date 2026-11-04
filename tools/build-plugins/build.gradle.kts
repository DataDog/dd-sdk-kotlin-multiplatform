/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import dev.detekt.gradle.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlintGradlePlugin)
    alias(libs.plugins.detektGradlePlugin)
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
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
    implementation(libs.ktlintGradlePlugin)
    implementation(libs.detektGradlePlugin)

    testImplementation(libs.bundles.jUnit5)
    testImplementation(libs.bundles.jvmTestTools)
}

ktlint {
    version = provider { libs.versions.ktlint.get() }
    filter {
        exclude { it.file.invariantSeparatorsPath.contains("/build/generated") }
    }
}

detekt {
    config.setFrom(file("../../detekt.yml"))
    // Keep in sync with `detekt-conventions`: excludes are matched relative to the repository root.
    basePath.set(layout.projectDirectory.dir("../..").asFile.normalize())
    buildUponDefaultConfig = false
    parallel = true
    source.setFrom(
        fileTree(layout.projectDirectory) {
            include("*.kts", "src/**/*.gradle.kts")
        }
    )
}

tasks.named<Detekt>("detektMain") {
    exclude("**/*.gradle.kts")
}

tasks.named("detekt") {
    dependsOn("detektMain", "detektTest")
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
    val buildDir = layout.buildDirectory.get().asFile
    exclude { it.file.startsWith(buildDir) }
    reports {
        sarif.required = false
        html.required = false
        markdown.required = false
        checkstyle.required = false
    }
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
        register("DatadogSpmBuildPlugin") {
            id = "datadog-ios-build-spm"
            implementationClass = "com.datadog.build.plugin.iosnative.DatadogSpmBuildPlugin"
        }
        register("DatadogFrameworksPlugin") {
            id = "datadog-ios-frameworks"
            implementationClass = "com.datadog.build.plugin.iosnative.DatadogFrameworksPlugin"
        }
        register("DetektConventionsPlugin") {
            id = "detekt-conventions"
            implementationClass = "com.datadog.build.plugin.detekt.DetektConventionsPlugin"
        }
    }
}
