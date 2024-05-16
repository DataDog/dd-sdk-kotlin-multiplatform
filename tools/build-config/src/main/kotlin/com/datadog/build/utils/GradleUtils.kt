/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.utils

import org.gradle.api.Project
import org.gradle.api.Task
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

inline fun <reified T : Task> Project.taskConfig(
    crossinline configure: T.() -> Unit
) {
    afterEvaluate {
        tasks.withType(T::class.java) { configure() }
    }
}

fun Project.execShell(vararg command: String): List<String> {
    val outputStream = ByteArrayOutputStream()
    this.exec {
        commandLine(*command)
        standardOutput = outputStream
    }

    val reader = InputStreamReader(ByteArrayInputStream(outputStream.toByteArray()))
    return reader.readLines()
}
