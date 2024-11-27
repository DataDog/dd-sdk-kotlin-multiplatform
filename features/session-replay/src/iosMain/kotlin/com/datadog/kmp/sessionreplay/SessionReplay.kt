/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay

import cocoapods.DatadogSessionReplay.DDSessionReplay
import cocoapods.DatadogSessionReplay.DDSessionReplayConfiguration
import com.datadog.kmp.sessionreplay.configuration.SessionReplayConfiguration

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
        DDSessionReplay.enableWith(sessionReplayConfiguration.nativeConfiguration as DDSessionReplayConfiguration)

    /**
     * Start recording session replay data.
     */
    actual fun startRecording() {
        DDSessionReplay.startRecording()
    }

    /**
     * Stop recording session replay data.
     */
    actual fun stopRecording() {
        DDSessionReplay.stopRecording()
    }
}
