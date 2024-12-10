/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration

import com.datadog.android.sessionreplay.ExtensionSupport
import com.datadog.android.sessionreplay.SystemRequirementsConfiguration
import com.datadog.kmp.sessionreplay.SessionReplay
import com.datadog.kmp.sessionreplay.configuration.internal.AndroidSessionReplayConfigurationBuilder
import com.datadog.kmp.sessionreplay.configuration.internal.PlatformSessionReplayConfigurationBuilder

/**
 * Adds an extension support implementation. This is mostly used when you want to provide
 * different behaviour of the Session Replay when using other Android UI frameworks
 * than the default ones.
 */
fun SessionReplayConfiguration.Builder.addExtensionSupport(
    extensionSupport: ExtensionSupport
): SessionReplayConfiguration.Builder {
    (platformBuilder as AndroidSessionReplayConfigurationBuilder).addExtensionSupport(extensionSupport)
    return this
}

/**
 * This option controls whether optimization is enabled or disabled for recording Session Replay data.
 * By default the value is true, meaning the dynamic optimization is enabled. When dynamic optimization is enabled, if
 * recording crosses the reasonable limit of device resources usage (e.g. CPU), than recording is throttled, which
 * may reduce the recording quality. When dynamic optimization is disabled, device resources usage is not capped.
 */
fun SessionReplayConfiguration.Builder.setDynamicOptimizationEnabled(
    dynamicOptimizationEnabled: Boolean
): SessionReplayConfiguration.Builder {
    (platformBuilder as AndroidSessionReplayConfigurationBuilder)
        .setDynamicOptimizationEnabled(dynamicOptimizationEnabled)
    return this
}

/**
 * Defines the minimum system requirements for enabling the Session Replay feature.
 * When [SessionReplay.enable] is invoked, the system configuration is verified against these requirements.
 * If the system meets the specified criteria, Session Replay will be successfully enabled.
 * If this function is not invoked, no minimum requirements will be enforced, and Session Replay will be
 * enabled on all devices.
 */
fun SessionReplayConfiguration.Builder.setSystemRequirements(
    systemRequirementsConfiguration: SystemRequirementsConfiguration
): SessionReplayConfiguration.Builder {
    (platformBuilder as AndroidSessionReplayConfigurationBuilder)
        .setSystemRequirements(systemRequirementsConfiguration)
    return this
}

internal actual fun platformConfigurationBuilder(sampleRate: Float): PlatformSessionReplayConfigurationBuilder<*> =
    AndroidSessionReplayConfigurationBuilder(sampleRate)
