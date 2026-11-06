/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.config

import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

interface DatadogBuildConfigExtension {
    val jvmTarget: Property<JvmTarget>
    val kotlinVersion: Property<KotlinVersion>
    val pomDescription: Property<String>
}

internal val DatadogBuildConfigExtension.jvmTargetOrDefaultProvider: Provider<JvmTarget>
    get() = jvmTarget.orElse(JvmTarget.JVM_17)

internal val DatadogBuildConfigExtension.jvmTargetOrDefault
    get() = jvmTargetOrDefaultProvider.get()

internal val DatadogBuildConfigExtension.kotlinVersionOrDefault
    get() = kotlinVersion.getOrElse(KotlinVersion.KOTLIN_2_2)
