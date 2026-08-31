/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.iosnative.tasks

import com.datadog.build.plugin.iosnative.DatadogPodsBuildPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateSyntheticDatadogPodspecTask : DefaultTask() {

    @get:Input
    abstract val podVersion: Property<String>

    @get:Input
    @get:Optional
    abstract val localSourcePath: Property<String>

    @get:Input
    abstract val iosDeploymentTarget: Property<String>

    @get:Input
    abstract val tvosDeploymentTarget: Property<String>

    @get:Input
    abstract val iosPods: ListProperty<String>

    @get:Input
    abstract val tvosPods: ListProperty<String>

    @get:Input
    abstract val syntheticPodVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val rootDirFile = outputDirectory.get().asFile
        val sourcesDir = File(rootDirFile, "Sources")
        sourcesDir.mkdirs()

        val localDatadogSourcePath = localSourcePath.orNull?.let(::File)
        require(localDatadogSourcePath == null || localDatadogSourcePath.isDirectory) {
            "Datadog iOS local source directory does not exist: $localDatadogSourcePath"
        }
        val iosPodspecFile = File(rootDirFile, "${DatadogPodsBuildPlugin.SYNTHETIC_IOS_TARGET_NAME}Root.podspec")
        val iosPodDependencies = podDependencies(iosPods.get(), localDatadogSourcePath)

        iosPodspecFile.writeText(
            """
            |Pod::Spec.new do |s|
            |  s.name = '${DatadogPodsBuildPlugin.SYNTHETIC_IOS_TARGET_NAME}Root'
            |  s.version = '${syntheticPodVersion.get()}'
            |  s.summary = 'Synthetic Datadog pod to prebuild native dependencies for KMP.'
            |  s.homepage = 'https://www.datadoghq.com/'
            |  s.license = { :type => 'Apache-2.0' }
            |  s.author = { 'Datadog' => 'support@datadoghq.com' }
            |  s.source = { :path => '.' }
            |  s.ios.deployment_target = '${iosDeploymentTarget.get()}'
            |  s.tvos.deployment_target = '${tvosDeploymentTarget.get()}'
            |  s.source_files = 'Sources/**/*.{h,m,mm,swift}'
            |
            |$iosPodDependencies
            |end
            |
            """.trimMargin()
        )

        val tvosPodspecFile = File(rootDirFile, "${DatadogPodsBuildPlugin.SYNTHETIC_TVOS_TARGET_NAME}Root.podspec")
        val tvosPodDependencies = podDependencies(tvosPods.get(), localDatadogSourcePath)
        tvosPodspecFile.writeText(
            """
            |Pod::Spec.new do |s|
            |  s.name = '${DatadogPodsBuildPlugin.SYNTHETIC_TVOS_TARGET_NAME}Root'
            |  s.version = '${syntheticPodVersion.get()}'
            |  s.summary = 'Synthetic Datadog pod to prebuild native dependencies for KMP.'
            |  s.homepage = 'https://www.datadoghq.com/'
            |  s.license = { :type => 'Apache-2.0' }
            |  s.author = { 'Datadog' => 'support@datadoghq.com' }
            |  s.source = { :path => '.' }
            |  s.tvos.deployment_target = '${tvosDeploymentTarget.get()}'
            |  s.source_files = 'Sources/**/*.{h,m,mm,swift}'
            |
            |$tvosPodDependencies
            |end
            |
            """.trimMargin()
        )

        File(sourcesDir, "placeholder.m").writeText(
            """
            |void datadog_synthetic_pod_placeholder(void) {}
            |
            """.trimMargin()
        )

        File(rootDirFile, "Podfile").writeText(
            """
            |source 'https://cdn.cocoapods.org/'
            |install! 'cocoapods', :integrate_targets => false
            |use_frameworks! :linkage => :static
            |
            |target '${DatadogPodsBuildPlugin.SYNTHETIC_IOS_TARGET_NAME}' do
            |  platform :ios, '${iosDeploymentTarget.get()}'
            |  pod '${DatadogPodsBuildPlugin.SYNTHETIC_IOS_TARGET_NAME}Root', :path => '.'
            |${localPodDeclarations(iosPods.get(), localDatadogSourcePath)}
            |end
            |
            |target '${DatadogPodsBuildPlugin.SYNTHETIC_TVOS_TARGET_NAME}' do
            |  platform :tvos, '${tvosDeploymentTarget.get()}'
            |  pod '${DatadogPodsBuildPlugin.SYNTHETIC_TVOS_TARGET_NAME}Root', :path => '.'
            |${localPodDeclarations(tvosPods.get(), localDatadogSourcePath)}
            |end
            |
            |post_install do |installer|
            |  installer.pods_project.targets.each do |target|
            |    target.build_configurations.each do |config|
            |      config.build_settings['EXPANDED_CODE_SIGN_IDENTITY'] = ""
            |      config.build_settings['CODE_SIGNING_REQUIRED'] = "NO"
            |      config.build_settings['CODE_SIGNING_ALLOWED'] = "NO"
            |    end
            |  end
            |end
            |
            """.trimMargin()
        )
    }

    private fun podDependencies(pods: List<String>, localSourcePath: File?): String {
        if (localSourcePath != null) return ""
        return pods.joinToString(separator = "\n") { "  s.dependency '$it', '${podVersion.get()}'" }
    }

    private fun localPodDeclarations(pods: List<String>, localSourcePath: File?): String {
        if (localSourcePath == null) return ""
        val path = localSourcePath.absolutePath.replace("'", "\\\\'")
        return (pods + "DatadogInternal")
            .distinct()
            .joinToString(separator = "\n") { "  pod '$it', :path => '$path'" }
    }
}
