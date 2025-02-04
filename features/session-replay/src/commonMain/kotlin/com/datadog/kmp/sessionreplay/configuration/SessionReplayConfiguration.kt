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
     * A Builder class for a [SessionReplayConfiguration]. See more methods in platform-specific source sets.
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
        @Deprecated(
            message = "This method is deprecated and will be removed in future versions. " +
                "Use the new Fine Grained Masking APIs instead: " +
                "[setImagePrivacy], [setTouchPrivacy], [setTextAndInputPrivacy]."
        )
        fun setPrivacy(privacy: SessionReplayPrivacy): Builder {
            platformBuilder.setPrivacy(privacy)
            return this
        }

        /**
         * Sets the image recording level for the Session Replay feature.
         * If not specified then all images that are considered to be content images will be masked by default.
         * @see ImagePrivacy.MASK_NONE
         * @see ImagePrivacy.MASK_LARGE_ONLY
         * @see ImagePrivacy.MASK_ALL
         */
        fun setImagePrivacy(privacy: ImagePrivacy): Builder {
            platformBuilder.setImagePrivacy(privacy)
            return this
        }

        /**
         * Sets the touch recording level for the Session Replay feature.
         * If not specified then all touches will be hidden by default.
         * @see TouchPrivacy.HIDE
         * @see TouchPrivacy.SHOW
         */
        fun setTouchPrivacy(privacy: TouchPrivacy): Builder {
            platformBuilder.setTouchPrivacy(privacy)
            return this
        }

        /**
         * Sets the text and input recording level for the Session Replay feature.
         * If not specified then sensitive text will be masked by default.
         * @see TextAndInputPrivacy.MASK_SENSITIVE_INPUTS
         * @see TextAndInputPrivacy.MASK_ALL_INPUTS
         * @see TextAndInputPrivacy.MASK_ALL
         */
        fun setTextAndInputPrivacy(privacy: TextAndInputPrivacy): Builder {
            platformBuilder.setTextAndInputPrivacy(privacy)
            return this
        }

        /**
         * Should recording start automatically (or be manually started).
         * If not specified then by default it starts automatically.
         * @param enabled whether recording should start automatically or not.
         */
        fun startRecordingImmediately(enabled: Boolean): Builder {
            platformBuilder.startRecordingImmediately(enabled)
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
