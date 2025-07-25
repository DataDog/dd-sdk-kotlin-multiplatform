import com.datadog.build.ProjectConfig
import com.datadog.build.utils.taskConfig
import io.github.gradlenexus.publishplugin.AbstractNexusStagingRepositoryTask

/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    // trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinCocoapods) apply false
    alias(libs.plugins.dependencyLicense) apply false
    alias(libs.plugins.mokkery) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.nexusPublish)
    // false - just to load classes into a classpath
    id("datadog-build-config") apply false
}

nexusPublishing {
    repositories {
        sonatype {
            val sonatypeUsername = System.getenv("CENTRAL_PUBLISHER_USERNAME")
            val sonatypePassword = System.getenv("CENTRAL_PUBLISHER_PASSWORD")
            stagingProfileId.set("378eecbbe2cf9")
            if (sonatypeUsername != null) username.set(sonatypeUsername)
            if (sonatypePassword != null) password.set(sonatypePassword)
            // see https://github.com/gradle-nexus/publish-plugin#publishing-to-maven-central-via-sonatype-central
            // For official documentation:
            // staging repo publishing https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
        }
    }
}

// nexus-publish plugin creates repository tasks only for the root project, so we cannot set a
// sub-project-specific description
project.taskConfig<AbstractNexusStagingRepositoryTask> {
    repositoryDescription.set(
        "${ProjectConfig.GROUP_ID}:dd-sdk-kotlin-multiplatform:${ProjectConfig.VERSION.name}"
    )
}

/**
 * Task necessary to be compliant with the shared Android static analysis pipeline
 */
tasks.register("checkGeneratedFiles") { }

/**
 * Creates a task aggregating a task on all projects.
 * @param aggregationTaskName the name of the aggregating task
 * @param projectTaskName the name of the task to aggregate from each module
 * @param pluginName the name of the plugin to expect on modules
 */
fun registerPluginSpecificAggregationTask(
    aggregationTaskName: String,
    projectTaskName: String,
    pluginName: String
) {
    tasks.register(aggregationTaskName) {
        val aggregationTask = this
        allprojects.forEach {
            it.pluginManager.withPlugin(pluginName) {
                val projectTask = it.tasks.named(projectTaskName)
                aggregationTask.dependsOn(projectTask)
            }
        }
    }
}

registerPluginSpecificAggregationTask(
    "checkApiSurfaceChangesAll",
    "checkApiSurfaceChanges",
    "api-surface"
)
registerPluginSpecificAggregationTask(
    "generateApiSurfaceAll",
    "generateApiSurface",
    "api-surface"
)

registerPluginSpecificAggregationTask(
    "listTransitiveDependenciesAll",
    "listTransitiveDependencies",
    "transitive-dependencies"
)

val publishableProjects = listOf(
    projects.core,
    projects.features.logs,
    projects.features.rum,
    projects.features.webview,
    projects.features.sessionReplay,
    projects.integrations.ktor,
    projects.integrations.ktor3
)

val jvmUnitTestDebugAllTask = tasks.register("jvmUnitTestDebugAll") {
    val subProjectsTestTasks = publishableProjects.map {
        "${it.targetProjectIdentity.buildTreePath.path}:testDebugUnitTest"
    }
    dependsOn(subProjectsTestTasks)
}

val jvmUnitTestReleaseAllTask = tasks.register("jvmUnitTestReleaseAll") {
    val subProjectsTestTasks = publishableProjects.map {
        "${it.targetProjectIdentity.buildTreePath.path}:testReleaseUnitTest"
    }
    dependsOn(subProjectsTestTasks)
}

// will cover Android-specific tests + tests from common source set
val jvmUnitTestAllTask = tasks.register("jvmUnitTestAll") {
    dependsOn(
        jvmUnitTestDebugAllTask,
        jvmUnitTestReleaseAllTask,
        gradle.includedBuild("build-plugins").task(":test")
    )
}

val iosUnitTestAllTask = tasks.register("iosUnitTestAll") {
    val subProjectsTestTasks = publishableProjects.map {
        "${it.targetProjectIdentity.buildTreePath.path}:iosSimulatorArm64Test"
    }
    dependsOn(subProjectsTestTasks)
}

val tvosUnitTestAllTask = tasks.register("tvosUnitTestAll") {
    val subProjectsTestTasks = publishableProjects
        // by some reason something like `projects.features.webview == projects.features.webview` evaluates to false
        .filter {
            it.targetProjectIdentity.buildTreePath.path !in setOf(":features:session-replay", ":features:webview")
        }
        .map { "${it.targetProjectIdentity.buildTreePath.path}:tvosSimulatorArm64Test" }
    dependsOn(subProjectsTestTasks)
}

val appleUnitTestAllTask = tasks.register("appleUnitTestAll") {
    dependsOn(iosUnitTestAllTask, tvosUnitTestAllTask)
}

tasks.register("unitTestAll") {
    dependsOn(jvmUnitTestAllTask, appleUnitTestAllTask)
}

tasks.register("lintCheckAll") {
    val subProjectsLintTasks = publishableProjects.map {
        "${it.targetProjectIdentity.buildTreePath.path}:lintRelease"
    }
    dependsOn(subProjectsLintTasks)
}
