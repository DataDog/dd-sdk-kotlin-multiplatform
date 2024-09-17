/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration

import com.datadog.kmp.sessionreplay.configuration.internal.PlatformSessionReplayConfigurationBuilder

/**
 * Describes configuration to be used for the Session Replay feature.
 */
data class SessionReplayConfiguration internal constructor(
    internal val nativeConfiguration: Any
) {

    /**
     * A Builder class for a [SessionReplayConfiguration].
     */
    class Builder {

        internal val platformBuilder: PlatformSessionReplayConfigurationBuilder<*>

        /**
         * Creates a new instance of [Builder].
         * @param sampleRate must be a value between 0 and 100. A value of 0
         * means no session will be recorded, 100 means all sessions will be recorded.
         */
        constructor(sampleRate: Float) : this(platformConfigurationBuilder(sampleRate))

        internal constructor(platformBuilder: PlatformSessionReplayConfigurationBuilder<*>) {
            this.platformBuilder = platformBuilder
        }

        /**
         * Sets the privacy rule for the Session Replay feature.
         * If not specified all the elements will be masked by default (MASK).
         * @see SessionReplayPrivacy.ALLOW
         * @see SessionReplayPrivacy.MASK
         * @see SessionReplayPrivacy.MASK_USER_INPUT
         */
        fun setPrivacy(privacy: SessionReplayPrivacy): Builder {
            platformBuilder.setPrivacy(privacy)
            return this
        }

        /**
         * Builds a [SessionReplayConfiguration] based on the current state of this Builder.
         */
        fun build(): SessionReplayConfiguration {
            return SessionReplayConfiguration(
                platformBuilder.build()
            )
        }
    }
}

internal expect fun platformConfigurationBuilder(sampleRate: Float): PlatformSessionReplayConfigurationBuilder<*>
