/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.iosnative

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import org.jetbrains.kotlin.konan.target.Family
import javax.inject.Inject

abstract class DatadogNativeFrameworkSpec @Inject constructor(
    val name: String,
    objects: ObjectFactory
) {
    val linkOnly: Property<Boolean> = objects.property<Boolean>()
        .convention(false)
    val packageName: Property<String> = objects.property<String>()
        .convention("cocoapods.$name")
    val compilerOpts: ListProperty<String> = objects.listProperty<String>()
        .convention(listOf("-fmodules"))
    val targetFamilies: SetProperty<Family> = objects.setProperty<Family>()
        .convention(setOf(Family.IOS, Family.TVOS))
}

abstract class DatadogFrameworksExtension @Inject constructor(objects: ObjectFactory) {
    val rootBuildTaskName: Property<String> = objects.property<String>()
        .convention("buildDatadogPods")
    val iosUmbrellaFramework: Property<String> = objects.property<String>()
        .convention("Pods_${DatadogPodsBuildPlugin.SYNTHETIC_IOS_TARGET_NAME}")
    val tvosUmbrellaFramework: Property<String> = objects.property<String>()
        .convention("Pods_${DatadogPodsBuildPlugin.SYNTHETIC_TVOS_TARGET_NAME}")
    val extraFrameworks: ListProperty<String> = objects.listProperty<String>()
        .convention(emptyList())
    val includeSwiftCompatibilityWorkaround: Property<Boolean> = objects.property<Boolean>()
        .convention(true)
    val includeObjcCategoryLinkerFlag: Property<Boolean> = objects.property<Boolean>()
        .convention(false)

    val frameworks: NamedDomainObjectContainer<DatadogNativeFrameworkSpec> =
        objects.domainObjectContainer(DatadogNativeFrameworkSpec::class) { name ->
            objects.newInstance(name)
        }

    fun framework(name: String, configure: DatadogNativeFrameworkSpec.() -> Unit) {
        configure.invoke(frameworks.maybeCreate(name))
    }
}
