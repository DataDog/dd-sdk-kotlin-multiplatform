/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.core.configuration

import com.datadog.kmp.Datadog
import com.datadog.kmp.DatadogSite

/**
 * An object describing the configuration of the Datadog SDK.
 *
 * This is necessary to initialize the SDK with the [Datadog.initialize] method.
 */
data class Configuration
internal constructor(
    internal val coreConfig: Core,
    internal val clientToken: String,
    internal val env: String,
    internal val variant: String,
    internal val service: String?
) {

    internal data class Core(
        val batchSize: BatchSize,
        val uploadFrequency: UploadFrequency,
        val site: DatadogSite,
        val batchProcessingLevel: BatchProcessingLevel,
        val trackCrashes: Boolean,
        val proxyConfiguration: ProxyConfiguration?
    )

    // region Builder

    /**
     * A Builder class for a [Configuration].
     *
     * @param clientToken your API key of type Client Token
     * @param env the environment name that will be sent with each event. This can be used to
     * filter your events on different environments (e.g.: "staging" vs. "production").
     * @param variant the variant of your application, which should be the value from your
     * `BuildConfig.FLAVOR` constant if you have different flavors, empty string otherwise.
     * @param service the service name (if set to null, it'll be set to your application's
     * package name, e.g.: com.example.android)
     */
    @Suppress("TooManyFunctions")
    class Builder(
        private val clientToken: String,
        private val env: String,
        private val variant: String = NO_VARIANT,
        private val service: String? = null
    ) {

        private var coreConfig = DEFAULT_CORE_CONFIG

        /**
         * Builds a [Configuration] based on the current state of this Builder.
         */
        fun build(): Configuration {
            return Configuration(
                coreConfig = coreConfig,
                clientToken = clientToken,
                env = env,
                variant = variant,
                service = service
            )
        }

        /**
         * Let the SDK target your preferred Datadog's site.
         */
        fun useSite(site: DatadogSite): Builder {
            coreConfig = coreConfig.copy(site = site)
            return this
        }

        /**
         * Defines the batch size (impacts the size and number of requests performed by Datadog).
         * @param batchSize the desired batch size
         */
        fun setBatchSize(batchSize: BatchSize): Builder {
            coreConfig = coreConfig.copy(batchSize = batchSize)
            return this
        }

        /**
         * Defines the preferred upload frequency.
         * @param uploadFrequency the desired upload frequency policy
         */
        fun setUploadFrequency(uploadFrequency: UploadFrequency): Builder {
            coreConfig = coreConfig.copy(uploadFrequency = uploadFrequency)
            return this
        }

        /**
         * Defines the Batch processing level, defining the maximum number of batches processed
         * sequentially without a delay within one reading/uploading cycle.
         * @param batchProcessingLevel the desired batch processing level. By default it's set to
         * [BatchProcessingLevel.MEDIUM].
         * @see BatchProcessingLevel
         */
        fun setBatchProcessingLevel(batchProcessingLevel: BatchProcessingLevel): Builder {
            coreConfig = coreConfig.copy(batchProcessingLevel = batchProcessingLevel)
            return this
        }

        /**
         * Controls if Android (JVM)/iOS crashes are tracked or not. Default value is `true`.
         *
         * @param enabled whether crashes are tracked and sent to Datadog. Crash reporting should be enabled to be
         * able to symbolicate non-crash errors on iOS.
         */
        fun trackCrashes(enabled: Boolean): Builder {
            coreConfig = coreConfig.copy(trackCrashes = enabled)
            return this
        }

        /**
         * Enables a custom proxy for uploading tracked data to Datadog's intake.
         * @param proxyConfiguration the [ProxyConfiguration]
         */
        fun setProxy(proxyConfiguration: ProxyConfiguration): Builder {
            coreConfig = coreConfig.copy(proxyConfiguration = proxyConfiguration)
            return this
        }
    }

    // endregion

    private companion object {

        /**
         * Value to use if application doesn't have flavors.
         */
        private const val NO_VARIANT: String = ""

        private val DEFAULT_CORE_CONFIG = Core(
            batchSize = BatchSize.MEDIUM,
            uploadFrequency = UploadFrequency.AVERAGE,
            site = DatadogSite.US1,
            batchProcessingLevel = BatchProcessingLevel.MEDIUM,
            trackCrashes = true,
            proxyConfiguration = null
        )
    }
}
