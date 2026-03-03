package com.datadog.build.plugin.iosnative.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateDatadogCInteropDefsTask : DefaultTask() {

    @get:Input
    abstract val frameworkNames: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputDir = outputDirectory.get().asFile
        outputDir.mkdirs()

        frameworkNames.get().forEach { frameworkName ->
            val defFile = outputDir.resolve("$frameworkName.def")
            defFile.writeText(
                """
                |language = Objective-C
                |modules = $frameworkName
                |linkerOpts = -framework $frameworkName
                |
                """.trimMargin()
            )
        }
    }
}
