package com.datadog.build.plugin.iosnative

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class DatadogPodsBuildExtension @Inject constructor(objects: ObjectFactory) {
    val podVersion: Property<String> = objects.property<String>()
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
