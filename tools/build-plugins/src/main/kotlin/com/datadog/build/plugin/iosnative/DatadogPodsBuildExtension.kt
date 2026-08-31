/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.iosnative

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class DatadogPodsBuildExtension @Inject constructor(objects: ObjectFactory) {
    val podVersion: Property<String> = objects.property<String>()
    val localSourcePath: Property<String> = objects.property<String>()
    val iosDeploymentTarget: Property<String> = objects.property<String>()
        .convention("12.0")
    val tvosDeploymentTarget: Property<String> = objects.property<String>()
        .convention("12.0")
    val iosPods: ListProperty<String> = objects.listProperty<String>()
        .convention(
            listOf(
                "DatadogCore",
                "DatadogCrashReporting",
                "DatadogLogs",
                "DatadogRUM",
                "DatadogWebViewTracking",
                "DatadogSessionReplay"
            )
        )
    val tvosPods: ListProperty<String> = objects.listProperty<String>()
        .convention(
            listOf(
                "DatadogCore",
                "DatadogCrashReporting",
                "DatadogLogs",
                "DatadogRUM"
            )
        )
}
