/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.rum.configuration.internal.CombinedRumSessionListener
import com.datadog.kmp.rum.configuration.internal.PlatformRumConfigurationBuilder
import com.datadog.kmp.rum.event.ViewEventMapper
import com.datadog.kmp.rum.model.ActionEvent
import com.datadog.kmp.rum.model.ErrorEvent
import com.datadog.kmp.rum.model.LongTaskEvent
import com.datadog.kmp.rum.model.ResourceEvent
import com.datadog.kmp.rum.model.ViewEvent

/**
 * Describes configuration to be used for the RUM feature.
 */
class RumConfiguration internal constructor(internal val nativeConfiguration: Any) {

    /**
     * A Builder class for a [RumConfiguration].
     */
    @Suppress("TooManyFunctions")
    class Builder {

        internal val platformBuilder: PlatformRumConfigurationBuilder<*>
        private var userSessionListener: RumSessionListener? = null

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
            this.userSessionListener = sessionListener
            return this
        }

        /**
         * Sets the [ViewEventMapper] for the RUM [ViewEvent]. You can use this interface implementation
         * to modify the [ViewEvent] attributes before serialisation.
         *
         * @param eventMapper the [ViewEventMapper] implementation.
         */
        fun setViewEventMapper(eventMapper: ViewEventMapper): Builder {
            platformBuilder.setViewEventMapper(eventMapper = eventMapper)
            return this
        }

        /**
         * Sets the [EventMapper] for the RUM [ResourceEvent]. You can use this interface implementation
         * to modify the [ResourceEvent] attributes before serialisation.
         *
         * @param eventMapper the [EventMapper] implementation.
         */
        fun setResourceEventMapper(eventMapper: EventMapper<ResourceEvent>): Builder {
            platformBuilder.setResourceEventMapper(eventMapper = eventMapper)
            return this
        }

        /**
         * Sets the [EventMapper] for the RUM [ActionEvent]. You can use this interface implementation
         * to modify the [ActionEvent] attributes before serialisation.
         *
         * @param eventMapper the [EventMapper] implementation.
         */
        fun setActionEventMapper(eventMapper: EventMapper<ActionEvent>): Builder {
            platformBuilder.setActionEventMapper(eventMapper = eventMapper)
            return this
        }

        /**
         * Sets the [EventMapper] for the RUM [ErrorEvent]. You can use this interface implementation
         * to modify the [ErrorEvent] attributes before serialisation.
         *
         * @param eventMapper the [EventMapper] implementation.
         */
        fun setErrorEventMapper(eventMapper: EventMapper<ErrorEvent>): Builder {
            platformBuilder.setErrorEventMapper(eventMapper = eventMapper)
            return this
        }

        /**
         * Sets the [EventMapper] for the RUM [LongTaskEvent]. You can use this interface implementation
         * to modify the [LongTaskEvent] attributes before serialisation.
         *
         * @param eventMapper the [EventMapper] implementation.
         */
        fun setLongTaskEventMapper(eventMapper: EventMapper<LongTaskEvent>): Builder {
            platformBuilder.setLongTaskEventMapper(eventMapper = eventMapper)
            return this
        }

        /**
         * Enables/Disables collection of an anonymous user ID across sessions.
         *
         * By default, the SDK generates a unique, non-personal anonymous user ID that is
         * persisted across app launches. This ID is attached to each RUM session, allowing
         * to link sessions originating from the same user/device without collecting personal data.
         */
        fun trackAnonymousUser(enabled: Boolean): Builder {
            platformBuilder.trackAnonymousUser(enabled)
            return this
        }

        /**
         * Let the RUM feature target a custom server.
         * The provided url should be the full endpoint url, e.g.: https://example.com/rum/upload
         *
         * This is a configuration for the reverse proxy, unlike
         * [com.datadog.kmp.core.configuration.Configuration.Builder.setProxy] which is a configuration for the
         * forward proxy. Prefer to use [com.datadog.kmp.core.configuration.Configuration.Builder.setProxy] instead if
         * possible.
         */
        fun useCustomEndpoint(endpoint: String): Builder {
            platformBuilder.useCustomEndpoint(endpoint)
            return this
        }

        /**
         * Builds a [RumConfiguration] based on the current state of this Builder.
         */
        fun build(): RumConfiguration {
            val internalSessionListener = InternalRumSessionProvider
            val combinedListener = CombinedRumSessionListener(internalSessionListener, userSessionListener)

            platformBuilder.setSessionListener(combinedListener)

            return RumConfiguration(platformBuilder.build())
        }

        private companion object {
            const val DEFAULT_LONG_TASK_THRESHOLD_MS = 100L
        }
    }
}

internal expect fun platformConfigurationBuilder(applicationId: String): PlatformRumConfigurationBuilder<Any>
