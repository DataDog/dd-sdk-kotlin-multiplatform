/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

/**
 * Configuration for slow frames monitoring.
 *
 * This class defines thresholds for frame duration to classify frames based on their duration type,
 * providing various statistics to assist in identifying UI performance issues such as slow frames,
 * slow frame rate, and freeze rate.
 *
 * @param maxSlowFramesAmount The maximum number of slow frame records to track for each view.
 * The default value is 1000.
 * @param maxSlowFrameThresholdNs The threshold (in nanoseconds) used to classify a frame as slow.
 * Frames with durations exceeding this threshold will not be considered slow.
 * The default value is 700,000,000 ns (700 ms).
 * @param continuousSlowFrameThresholdNs The threshold (in nanoseconds) used to classify a frame
 * as continuously slow. If two consecutive slow frames are recorded and the delay between them is
 * less than this threshold, the previous frame record will be updated rather than adding a new one.
 * The default value is 16,666,666 ns (approximately 1/60 fps).
 * @param freezeDurationThresholdNs The duration (in nanoseconds) used to classify a frame as a
 * frozen frame. The default value is 5,000,000,000 ns (5 seconds).
 * @param minViewLifetimeThresholdNs The minimum lifetime (in nanoseconds) a view must have before
 * it is considered for monitoring. The default value is 1,000,000,000 ns (1 s).
 */
data class SlowFramesConfiguration(
    val maxSlowFramesAmount: Int = DEFAULT_SLOW_FRAME_RECORDS_MAX_AMOUNT,
    val maxSlowFrameThresholdNs: Long = DEFAULT_FROZEN_FRAME_THRESHOLD_NS,
    val continuousSlowFrameThresholdNs: Long = DEFAULT_CONTINUOUS_SLOW_FRAME_THRESHOLD_NS,
    val freezeDurationThresholdNs: Long = DEFAULT_FREEZE_DURATION_NS,
    val minViewLifetimeThresholdNs: Long = DEFAULT_VIEW_LIFETIME_THRESHOLD_NS
) {

    companion object {
        /**
         * A default configuration with all parameters set to their default values.
         */
        val DEFAULT: SlowFramesConfiguration = SlowFramesConfiguration()

        private const val DEFAULT_SLOW_FRAME_RECORDS_MAX_AMOUNT: Int = 1000
        private const val DEFAULT_CONTINUOUS_SLOW_FRAME_THRESHOLD_NS: Long = 16_666_666L
        private const val DEFAULT_FROZEN_FRAME_THRESHOLD_NS: Long = 700_000_000
        private const val DEFAULT_FREEZE_DURATION_NS: Long = 5_000_000_000L
        private const val DEFAULT_VIEW_LIFETIME_THRESHOLD_NS: Long = 1_000_000_000L
    }
}
