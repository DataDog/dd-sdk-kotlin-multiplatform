package com.datadog.build.plugin.iosnative

import com.datadog.build.ProjectConfig
import com.datadog.build.plugin.iosnative.tasks.CheckNoDatadogPodsOutsideRootTask
import com.datadog.build.plugin.iosnative.tasks.GenerateSyntheticDatadogPodspecTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

class DatadogPodsBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<DatadogPodsBuildExtension>("datadogPodsBuild")

        val syntheticPodsRootDir = target.layout.buildDirectory.dir("datadog-pods")
        val podsBuildDir = target.layout.buildDirectory.dir("datadog-pods-build")

        val generateSyntheticDatadogPodspec = target.tasks.register<GenerateSyntheticDatadogPodspecTask>(
            "generateSyntheticDatadogPodspec"
        ) {
            outputDirectory.set(syntheticPodsRootDir)
            podVersion.set(extension.podVersion)
            iosDeploymentTarget.set(extension.iosDeploymentTarget)
            tvosDeploymentTarget.set(extension.tvosDeploymentTarget)
            iosPods.set(extension.iosPods)
            tvosPods.set(extension.tvosPods)
            syntheticPodVersion.set(ProjectConfig.VERSION.name)
        }

        val installDatadogPods = target.tasks.register<Exec>("installDatadogPods") {
            dependsOn(generateSyntheticDatadogPodspec)
            workingDir(syntheticPodsRootDir)
            commandLine(findPodExecutable(), "install", "--repo-update")
            inputs.file(syntheticPodsRootDir.map { it.file("Podfile") })
            inputs.file(syntheticPodsRootDir.map { it.file("${SYNTHETIC_IOS_TARGET_NAME}Root.podspec") })
            inputs.file(syntheticPodsRootDir.map { it.file("${SYNTHETIC_TVOS_TARGET_NAME}Root.podspec") })
            outputs.file(syntheticPodsRootDir.map { it.file("Podfile.lock") })
            outputs.dir(syntheticPodsRootDir.map { it.dir("Pods") })
        }

        fun registerPodBuildTask(
            taskName: String,
            scheme: String,
            sdk: String,
            destination: String,
            outputSubdirectory: String
        ) = target.tasks.register<Exec>(taskName) {
            dependsOn(installDatadogPods)
            val outputDir = podsBuildDir.map { it.dir(outputSubdirectory) }
            workingDir(syntheticPodsRootDir)
            commandLine(
                "xcodebuild",
                "-quiet",
                "-project", "Pods/Pods.xcodeproj",
                "-scheme", scheme,
                "-configuration", "Release",
                "-sdk", sdk,
                "-destination", destination,
                "ONLY_ACTIVE_ARCH=NO",
                "SKIP_INSTALL=NO",
                "BUILD_LIBRARY_FOR_DISTRIBUTION=YES",
                "CONFIGURATION_BUILD_DIR=${outputDir.get().asFile.absolutePath}"
            )
            inputs.file(syntheticPodsRootDir.map { it.file("Podfile.lock") })
            outputs.dir(outputDir)
        }

        val buildDatadogIosDevicePods = registerPodBuildTask(
            taskName = "buildDatadogIosDevicePods",
            scheme = "Pods-$SYNTHETIC_IOS_TARGET_NAME",
            sdk = "iphoneos",
            destination = "generic/platform=iOS",
            outputSubdirectory = "ios-device"
        )

        val buildDatadogIosSimulatorPods = registerPodBuildTask(
            taskName = "buildDatadogIosSimulatorPods",
            scheme = "Pods-$SYNTHETIC_IOS_TARGET_NAME",
            sdk = "iphonesimulator",
            destination = "generic/platform=iOS Simulator",
            outputSubdirectory = "ios-simulator"
        )

        val buildDatadogTvosDevicePods = registerPodBuildTask(
            taskName = "buildDatadogTvosDevicePods",
            scheme = "Pods-$SYNTHETIC_TVOS_TARGET_NAME",
            sdk = "appletvos",
            destination = "generic/platform=tvOS",
            outputSubdirectory = "tvos-device"
        )

        val buildDatadogTvosSimulatorPods = registerPodBuildTask(
            taskName = "buildDatadogTvosSimulatorPods",
            scheme = "Pods-$SYNTHETIC_TVOS_TARGET_NAME",
            sdk = "appletvsimulator",
            destination = "generic/platform=tvOS Simulator",
            outputSubdirectory = "tvos-simulator"
        )

        target.tasks.register("buildDatadogPods") {
            group = "cocoapods"
            description = "Generates a synthetic podspec, installs Datadog pods, and builds iOS/tvOS pods once at root."
            dependsOn(
                buildDatadogIosDevicePods,
                buildDatadogIosSimulatorPods,
                buildDatadogTvosDevicePods,
                buildDatadogTvosSimulatorPods
            )
        }

        target.tasks.register<CheckNoDatadogPodsOutsideRootTask>("forbidDatadogPodsOutsideRoot") {
            group = "verification"
            description = "Fails if Datadog pods are declared in subproject build scripts."
            val projectBuildFiles = target.provider {
                target.rootDir
                    .walkTopDown()
                    .filter { file ->
                        file.isFile &&
                            file.name == "build.gradle.kts" &&
                            file.parentFile != target.rootDir &&
                            !file.invariantSeparatorsPath.contains("/build/")
                    }
                    .toList()
            }
            buildFiles.setFrom(projectBuildFiles)
        }
    }

    private fun findPodExecutable(): String {
        val locations = listOf("/usr/local/bin/pod", "/opt/homebrew/bin/pod", "/usr/bin/pod")
        for (location in locations) {
            if (File(location).exists()) return location
        }
        return "pod"
    }

    companion object {
        const val SYNTHETIC_IOS_TARGET_NAME = "DatadogSyntheticIOS"
        const val SYNTHETIC_TVOS_TARGET_NAME = "DatadogSyntheticTVOS"
    }
}
