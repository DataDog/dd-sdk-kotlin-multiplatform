/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration

import com.datadog.android.sessionreplay.ExtensionSupport
import com.datadog.kmp.sessionreplay.configuration.internal.AndroidSessionReplayConfigurationBuilder
import com.datadog.kmp.sessionreplay.configuration.internal.PlatformSessionReplayConfigurationBuilder

/**
 * Adds an extension support implementation. This is mostly used when you want to provide
 * different behaviour of the Session Replay when using other Android UI frameworks
 * than the default ones.
 */
fun SessionReplayConfiguration.Builder.addExtensionSupport(extensionSupport: ExtensionSupport) =
    (platformBuilder as AndroidSessionReplayConfigurationBuilder).addExtensionSupport(extensionSupport)

internal actual fun platformConfigurationBuilder(sampleRate: Float): PlatformSessionReplayConfigurationBuilder<*> =
    AndroidSessionReplayConfigurationBuilder(sampleRate)
