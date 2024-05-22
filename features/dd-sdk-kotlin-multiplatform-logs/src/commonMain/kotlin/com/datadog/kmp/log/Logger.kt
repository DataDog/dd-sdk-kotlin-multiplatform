/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log

import com.datadog.kmp.log.internal.PlatformLogger
import com.datadog.kmp.log.internal.platformLoggerBuilder

/**
 * A class enabling Datadog logging features.
 *
 * It allows you to create a specific context (automatic information, custom attributes, tags) that
 * will be embedded in all logs sent through this logger.
 *
 * You can have multiple loggers configured in your application, each with their own settings.
 */
@Suppress("TooManyFunctions")
class Logger internal constructor(internal val platformLogger: PlatformLogger) {

    // region Log

    /**
     * Sends a Debug log message.
     * @param message the message to be logged
     * @param throwable a (nullable) throwable to be logged with the message. If you want to log platform-specific
     * error type, check extension methods available in platform-specific source sets.
     * @param attributes a map of attributes to include only for this message. If an attribute with
     * the same key already exist in this logger, it will be overridden (just for this message)
     */
    fun debug(
        message: String,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    ) {
        platformLogger.debug(message, throwable, attributes)
    }

    /**
     * Sends an Info log message.
     * @param message the message to be logged
     * @param throwable a (nullable) throwable to be logged with the message. If you want to log platform-specific
     * error type, check extension methods available in platform-specific source sets.
     * @param attributes a map of attributes to include only for this message. If an attribute with
     * the same key already exist in this logger, it will be overridden (just for this message)
     */
    fun info(
        message: String,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    ) {
        platformLogger.info(message, throwable, attributes)
    }

    /**
     * Sends a Warning log message.
     * @param message the message to be logged
     * @param throwable a (nullable) throwable to be logged with the message. If you want to log platform-specific
     * error type, check extension methods available in platform-specific source sets.
     * @param attributes a map of attributes to include only for this message. If an attribute with
     * the same key already exist in this logger, it will be overridden (just for this message)
     */
    fun warn(
        message: String,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    ) {
        platformLogger.warn(message, throwable, attributes)
    }

    /**
     * Sends an Error log message.
     * @param message the message to be logged
     * @param throwable a (nullable) throwable to be logged with the message. If you want to log platform-specific
     * error type, check extension methods available in platform-specific source sets.
     * @param attributes a map of attributes to include only for this message. If an attribute with
     * the same key already exist in this logger, it will be overridden (just for this message)
     */
    fun error(
        message: String,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    ) {
        platformLogger.error(message, throwable, attributes)
    }

    /**
     * Sends a Critical log message (Log.ASSERT for Android, Critical for iOS).
     * @param message the message to be logged
     * @param throwable a (nullable) throwable to be logged with the message. If you want to log platform-specific
     * error type, check extension methods available in platform-specific source sets.
     * @param attributes a map of attributes to include only for this message. If an attribute with
     * the same key already exist in this logger, it will be overridden (just for this message)
     */
    fun critical(
        message: String,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    ) {
        platformLogger.critical(message, throwable, attributes)
    }

    /**
     * Sends a log message with a given priority.
     * @param priority the log priority
     * @param message the message to be logged
     * @param throwable a (nullable) throwable to be logged with the message. If you want to log platform-specific
     * error type, check extension methods available in platform-specific source sets.
     * @param attributes a map of attributes to include only for this message. If an attribute with
     * the same key already exist in this logger, it will be overridden (just for this message)
     */
    fun log(
        priority: LogLevel,
        message: String,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    ) {
        platformLogger.log(priority, message, throwable, attributes)
    }

    // endregion

    // region Context Information (attributes, tags)

    /**
     * Add a custom attribute to all future logs sent by this logger.
     *
     * Values can be nested up to 10 levels deep. Keys
     * using more than 10 levels will be sanitized by SDK.
     *
     * @param key the key for this attribute
     * @param value the attribute value
     */
    fun addAttribute(key: String, value: Any?) {
        platformLogger.addAttribute(key, value)
    }

    /**
     * Remove a custom attribute from all future logs sent by this logger.
     * Previous logs won't lose the attribute value associated with this key if they were created
     * prior to this call.
     * @param key the key of the attribute to remove
     */
    fun removeAttribute(key: String) {
        platformLogger.removeAttribute(key)
    }

    /**
     * Add a tag to all future logs sent by this logger.
     * The tag will take the form "key:value".
     *
     * Tags must start with a letter and after that may contain the following characters:
     * Alphanumerics, Underscores, Minuses, Colons, Periods, Slashes. Other special characters
     * are converted to underscores.
     * Tags must be lowercase, and can be at most 200 characters. If the tag you provide is
     * longer, only the first 200 characters will be used.
     *
     * @param key the key for this tag
     * @param value the (non null) value of this tag
     * @see [documentation](https://docs.datadoghq.com/tagging/#defining-tags)
     */
    fun addTag(key: String, value: String) {
        platformLogger.addTag(key, value)
    }

    /**
     * Add a tag to all future logs sent by this logger.
     *
     * Tags must start with a letter and after that may contain the following characters:
     * Alphanumerics, Underscores, Minuses, Colons, Periods, Slashes. Other special characters
     * are converted to underscores.
     * Tags must be lowercase, and can be at most 200 characters. If the tag you provide is
     * longer, only the first 200 characters will be used.
     *
     * @param tag the (non null) tag
     * @see [documentation](https://docs.datadoghq.com/tagging/#defining-tags)
     */
    fun addTag(tag: String) {
        platformLogger.addTag(tag)
    }

    /**
     * Remove a tag from all future logs sent by this logger.
     * Previous logs won't lose the this tag if they were created prior to this call.
     * @param tag the tag to remove
     */
    fun removeTag(tag: String) {
        platformLogger.removeTag(tag)
    }

    /**
     * Remove all tags with the given key from all future logs sent by this logger.
     * Previous logs won't lose the this tag if they were created prior to this call.
     * @param key the key of the tags to remove
     */
    fun removeTagsWithKey(key: String) {
        platformLogger.removeTagsWithKey(key)
    }

    // endregion

    // region Builder

    /**
     * A Builder class for a [Logger].
     */
    class Builder {

        /**
         * Creates an instance of [Builder].
         */
        constructor() : this(platformLoggerBuilder())

        internal constructor(platformLoggerBuilder: PlatformLogger.Builder) {
            this.platformLoggerBuilder = platformLoggerBuilder
        }

        private val platformLoggerBuilder: PlatformLogger.Builder

        /**
         * Builds a [Logger] based on the current state of this Builder.
         */
        fun build(): Logger {
            return Logger(platformLoggerBuilder.build())
        }

        /**
         * Sets the service name that will appear in your logs.
         * @param service the service name (default = application package name)
         */
        fun setService(service: String): Builder {
            platformLoggerBuilder.setService(service)
            return this
        }

        /**
         * Sets a minimum threshold (priority) for the log to be sent to the Datadog servers. If log priority
         * is below this one, then it won't be sent. Default value is to allow all.
         * @param minLogThreshold Minimum log threshold to be sent to the Datadog servers.
         */
        fun setRemoteLogThreshold(minLogThreshold: LogLevel): Builder {
            platformLoggerBuilder.setRemoteLogThreshold(minLogThreshold)
            return this
        }

        /**
         * Enables your logs to be duplicated in LogCat (Android) or in debugger console (iOS).
         * @param enabled false by default
         */
        fun setPrintLogsToConsole(enabled: Boolean): Builder {
            platformLoggerBuilder.setPrintLogsToConsole(enabled)
            return this
        }

        /**
         * Enables network information to be automatically added in your logs.
         * @param enabled false by default
         */
        fun setNetworkInfoEnabled(enabled: Boolean): Builder {
            platformLoggerBuilder.setNetworkInfoEnabled(enabled)
            return this
        }

        /**
         * Sets the logger name that will appear in your logs when a throwable is attached.
         * @param name the logger custom name (default = application package name)
         */
        fun setName(name: String): Builder {
            platformLoggerBuilder.setName(name)
            return this
        }

        /**
         * Enables the logs bundling with the current active trace. If this feature is enabled all
         * the logs from this moment on will be bundled with the current trace and you will be able
         * to see all the logs sent during a specific trace.
         * @param enabled true by default
         */
        fun setBundleWithTraceEnabled(enabled: Boolean): Builder {
            platformLoggerBuilder.setBundleWithTraceEnabled(enabled)
            return this
        }

        /**
         * Enables the logs bundling with the current active View. If this feature is enabled all
         * the logs from this moment on will be bundled with the current view information and you
         * will be able to see all the logs sent during a specific view in the Rum Explorer.
         * @param enabled true by default
         */
        fun setBundleWithRumEnabled(enabled: Boolean): Builder {
            platformLoggerBuilder.setBundleWithRumEnabled(enabled)
            return this
        }

        /**
         * Sets the sample rate for this Logger.
         * @param sampleRate the sample rate, in percent.
         * A value of `30` means we'll send 30% of the logs. If value is `0`, no logs will be sent
         * to Datadog.
         * Default is 100.0 (ie: all logs are sent).
         */
        fun setRemoteSampleRate(sampleRate: Float): Builder {
            platformLoggerBuilder.setRemoteSampleRate(sampleRate)
            return this
        }

        // endregion
    }

    // endregion
}
