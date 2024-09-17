/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration.internal

import com.datadog.kmp.sessionreplay.configuration.SessionReplayPrivacy

internal interface PlatformSessionReplayConfigurationBuilder<T : Any> {

    fun setPrivacy(privacy: SessionReplayPrivacy)

    fun build(): T
}
