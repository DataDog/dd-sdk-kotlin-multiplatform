/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

import com.datadog.kmp.rum.configuration.internal.PlatformRumConfigurationBuilder

/**
 * Describes configuration to be used for the RUM feature.
 */
class RumConfiguration internal constructor(internal val nativeConfiguration: Any) {

    /**
     * A Builder class for a [RumConfiguration].
     */
    class Builder {

        internal val platformBuilder: PlatformRumConfigurationBuilder<*>

        /**
         * Creates a new instance of [Builder].
         *
         * @param applicationId your applicationId for RUM events
         */
        constructor(applicationId: String) : this(platformConfigurationBuilder(applicationId))

        internal constructor(platformBuilder: PlatformRumConfigurationBuilder<*>) {
            this.platformBuilder = platformBuilder
        }

        /**
         * Sets the sample rate for RUM Sessions.
         *
         * @param sampleRate the sample rate must be a value between 0 and 100. A value of 0
         * means no RUM event will be sent, 100 means all sessions will be kept.
         */
        fun setSessionSampleRate(sampleRate: Float): Builder {
            platformBuilder.setSessionSampleRate(sampleRate)
            return this
        }

        /**
         * Sets the sample rate for Internal Telemetry (info related to the work of the
         * SDK internals). Default value is 20.
         *
         * @param sampleRate the sample rate must be a value between 0 and 100. A value of 0
         * means no telemetry will be sent, 100 means all telemetry will be kept.
         */
        fun setTelemetrySampleRate(sampleRate: Float): Builder {
            platformBuilder.setTelemetrySampleRate(sampleRate)
            return this
        }

        /**
         * Enable long operations on the main thread to be tracked automatically.
         * Any long running operation on the main thread will appear as Long Tasks in Datadog
         * RUM Explorer
         * @param longTaskThresholdMs the threshold in milliseconds above which a task running on
         * the main thread is considered as a long task (default 100ms). Setting a
         * value less than or equal to 0 disables the long task tracking
         */
        fun trackLongTasks(longTaskThresholdMs: Long = DEFAULT_LONG_TASK_THRESHOLD_MS): Builder {
            platformBuilder.trackLongTasks(longTaskThresholdMs)
            return this
        }

        /**
         * Enables/Disables tracking RUM events when no there is no active foreground RUM view.
         *
         * By default, background events are not tracked. Enabling this feature might increase the
         * number of sessions tracked and impact your billing.
         *
         * @param enabled whether background events should be tracked in RUM.
         */
        fun trackBackgroundEvents(enabled: Boolean): Builder {
            platformBuilder.trackBackgroundEvents(enabled)
            return this
        }

        /**
         * Enables/Disables tracking of frustration signals.
         *
         * By default frustration signals are tracked. Currently the SDK supports detecting
         * error taps which occur when an error follows a user action tap.
         *
         * @param enabled whether frustration signals should be tracked in RUM.
         */
        fun trackFrustrations(enabled: Boolean): Builder {
            platformBuilder.trackFrustrations(enabled)
            return this
        }

        /**
         * Allows to specify the frequency at which to update the mobile vitals
         * data provided in the RUM View event.
         * @param frequency as [VitalsUpdateFrequency]
         * @see [VitalsUpdateFrequency]
         */
        fun setVitalsUpdateFrequency(frequency: VitalsUpdateFrequency): Builder {
            platformBuilder.setVitalsUpdateFrequency(frequency)
            return this
        }

        /**
         * Sets the Session listener.
         * @param sessionListener the listener to notify whenever a new Session starts.
         */
        fun setSessionListener(sessionListener: RumSessionListener): Builder {
            platformBuilder.setSessionListener(sessionListener)
            return this
        }

        /**
         * Builds a [RumConfiguration] based on the current state of this Builder.
         */
        fun build(): RumConfiguration {
            return RumConfiguration(platformBuilder.build())
        }

        private companion object {
            const val DEFAULT_LONG_TASK_THRESHOLD_MS = 100L
        }
    }
}

internal expect fun platformConfigurationBuilder(applicationId: String): PlatformRumConfigurationBuilder<Any>
