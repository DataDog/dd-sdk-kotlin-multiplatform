/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration.internal

import com.datadog.kmp.sessionreplay.configuration.ImagePrivacy
import com.datadog.kmp.sessionreplay.configuration.SessionReplayPrivacy
import com.datadog.kmp.sessionreplay.configuration.TextAndInputPrivacy
import com.datadog.kmp.sessionreplay.configuration.TouchPrivacy

internal interface PlatformSessionReplayConfigurationBuilder<T : Any> {

    fun setPrivacy(privacy: SessionReplayPrivacy)

    fun setImagePrivacy(privacy: ImagePrivacy)

    fun setTouchPrivacy(privacy: TouchPrivacy)

    fun setTextAndInputPrivacy(privacy: TextAndInputPrivacy)

    fun startRecordingImmediately(enabled: Boolean)

    fun build(): T
}
