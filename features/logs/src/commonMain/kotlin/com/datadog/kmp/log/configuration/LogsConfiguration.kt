/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.configuration

import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.log.configuration.internal.PlatformLogsConfigurationBuilder
import com.datadog.kmp.log.model.LogEvent

/**
 * Describes configuration to be used for the Logs feature.
 */
class LogsConfiguration internal constructor(internal val nativeConfiguration: Any) {

    /**
     * A Builder class for a [LogsConfiguration].
     */
    class Builder {

        internal val platformBuilder: PlatformLogsConfigurationBuilder<Any>

        /**
         * Creates a new instance of [Builder].
         */
        constructor() : this(platformLogsConfigurationBuilder())

        internal constructor(platformBuilder: PlatformLogsConfigurationBuilder<Any>) {
            this.platformBuilder = platformBuilder
        }

        /**
         * Sets the [EventMapper] for the [LogEvent].
         * You can use this interface implementation to modify the
         * [LogEvent] attributes before serialisation.
         *
         * @param eventMapper the [EventMapper] implementation.
         */
        fun setEventMapper(eventMapper: EventMapper<LogEvent>): Builder {
            platformBuilder.setEventMapper(eventMapper)
            return this
        }

        /**
         * Builds a [LogsConfiguration] based on the current state of this Builder.
         */
        fun build(): LogsConfiguration {
            return LogsConfiguration(platformBuilder.build())
        }
    }
}

internal expect fun platformLogsConfigurationBuilder(): PlatformLogsConfigurationBuilder<Any>
