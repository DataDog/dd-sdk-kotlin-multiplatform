/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay

import com.datadog.kmp.sessionreplay.configuration.SessionReplayConfiguration
import com.datadog.android.sessionreplay.SessionReplay as NativeSessionReplay
import com.datadog.android.sessionreplay.SessionReplayConfiguration as NativeSessionReplayConfiguration

/**
 * An entry point to Datadog Session Replay feature.
 */
actual object SessionReplay {

    /**
     * Enables a SessionReplay feature based on the configuration provided.
     *
     * @param sessionReplayConfiguration Configuration to use for the feature.
     */
    actual fun enable(sessionReplayConfiguration: SessionReplayConfiguration) =
        NativeSessionReplay.enable(sessionReplayConfiguration.nativeConfiguration as NativeSessionReplayConfiguration)

    /**
     * Start recording session replay data.
     */
    actual fun startRecording() {
        NativeSessionReplay.startRecording()
    }

    /**
     * Stop recording session replay data.
     */
    actual fun stopRecording() {
        NativeSessionReplay.stopRecording()
    }
}
