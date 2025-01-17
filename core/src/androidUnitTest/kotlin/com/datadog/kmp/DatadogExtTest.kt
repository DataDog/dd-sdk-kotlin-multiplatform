/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp

import com.datadog.kmp.core.configuration.BatchProcessingLevel
import com.datadog.kmp.core.configuration.BatchSize
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.core.configuration.UploadFrequency
import com.datadog.kmp.core.configuration.setProxy
import com.datadog.kmp.internal.InternalAttributes
import com.datadog.kmp.privacy.TrackingConsent
import com.datadog.kmp.tools.forge.Configurator
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.junit5.ForgeConfiguration
import fr.xgouchet.elmyr.junit5.ForgeExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import android.util.Log as AndroidLog
import com.datadog.android.DatadogSite as DatadogSiteAndroid
import com.datadog.android.core.configuration.BatchProcessingLevel as BatchProcessingLevelAndroid
import com.datadog.android.core.configuration.BatchSize as BatchSizeAndroid
import com.datadog.android.core.configuration.Configuration as ConfigurationAndroid
import com.datadog.android.core.configuration.UploadFrequency as UploadFrequencyAndroid
import com.datadog.android.privacy.TrackingConsent as TrackingConsentAndroid

@Extensions(ExtendWith(ForgeExtension::class))
@ForgeConfiguration(Configurator::class)
internal class DatadogExtTest {

    @Test
    fun `M return valid SDK value W SdkLogVerbosity_toSdkLogVerbosity`() {
        LOG_VERBOSITY_MAP.entries
            .associate { (key, value) -> value to key }
            .assertAllKeysEqualToValuesWhen { it.toSdkLogVerbosity }
    }

    @Test
    fun `M return valid native value W SdkLogVerbosity_native`() {
        LOG_VERBOSITY_MAP
            .assertAllKeysEqualToValuesWhen { it.native }
    }

    @Test
    fun `M return valid native value W TrackingConsent_native`() {
        mapOf(
            TrackingConsent.GRANTED to TrackingConsentAndroid.GRANTED,
            TrackingConsent.PENDING to TrackingConsentAndroid.PENDING,
            TrackingConsent.NOT_GRANTED to TrackingConsentAndroid.NOT_GRANTED
        )
            .assertExhaustive(TrackingConsent.entries)
            .assertAllKeysEqualToValuesWhen { it.native }
    }

    @Test
    fun `M return valid native value W UploadFrequency_native`() {
        mapOf(
            UploadFrequency.FREQUENT to UploadFrequencyAndroid.FREQUENT,
            UploadFrequency.AVERAGE to UploadFrequencyAndroid.AVERAGE,
            UploadFrequency.RARE to UploadFrequencyAndroid.RARE
        )
            .assertExhaustive(UploadFrequency.entries)
            .assertAllKeysEqualToValuesWhen { it.native }
    }

    @Test
    fun `M return valid native value W BatchProcessingLevel_native`() {
        mapOf(
            BatchProcessingLevel.HIGH to BatchProcessingLevelAndroid.HIGH,
            BatchProcessingLevel.MEDIUM to BatchProcessingLevelAndroid.MEDIUM,
            BatchProcessingLevel.LOW to BatchProcessingLevelAndroid.LOW
        )
            .assertExhaustive(BatchProcessingLevel.entries)
            .assertAllKeysEqualToValuesWhen { it.native }
    }

    @Test
    fun `M return valid native value W BatchSize_native`() {
        mapOf(
            BatchSize.LARGE to BatchSizeAndroid.LARGE,
            BatchSize.MEDIUM to BatchSizeAndroid.MEDIUM,
            BatchSize.SMALL to BatchSizeAndroid.SMALL
        )
            .assertExhaustive(BatchSize.entries)
            .assertAllKeysEqualToValuesWhen { it.native }
    }

    @Test
    fun `M return valid native value W DatadogSite_native`() {
        mapOf(
            DatadogSite.US1 to DatadogSiteAndroid.US1,
            DatadogSite.US1_FED to DatadogSiteAndroid.US1_FED,
            DatadogSite.US3 to DatadogSiteAndroid.US3,
            DatadogSite.US5 to DatadogSiteAndroid.US5,
            DatadogSite.EU1 to DatadogSiteAndroid.EU1,
            DatadogSite.AP1 to DatadogSiteAndroid.AP1
        )
            .assertExhaustive(DatadogSite.entries)
            .assertAllKeysEqualToValuesWhen { it.native }
    }

    @Test
    fun `M return expected configuration W Configuration_native`(@Forgery sdkConfiguration: Configuration) {
        // Given
        val coreConfig = sdkConfiguration.coreConfig
        val expectedConfiguration = ConfigurationAndroid.Builder(
            clientToken = sdkConfiguration.clientToken,
            env = sdkConfiguration.env,
            variant = sdkConfiguration.variant,
            service = sdkConfiguration.service
        )
            .useSite(coreConfig.site.native)
            .setBatchSize(coreConfig.batchSize.native)
            .setUploadFrequency(coreConfig.uploadFrequency.native)
            .setBatchProcessingLevel(coreConfig.batchProcessingLevel.native)
            .setCrashReportsEnabled(coreConfig.trackCrashes)
            .setAdditionalConfiguration(
                mapOf(
                    InternalAttributes.SOURCE_ATTRIBUTE,
                    InternalAttributes.SDK_VERSION_ATTRIBUTE
                )
            )
            .setProxy(coreConfig.proxyConfiguration)
            .build()

        // When
        val actualConfiguration = sdkConfiguration.native

        // Then
        assertThat(actualConfiguration).isEqualTo(expectedConfiguration)
    }

    companion object {
        private val LOG_VERBOSITY_MAP = mapOf(
            SdkLogVerbosity.DEBUG to AndroidLog.DEBUG,
            SdkLogVerbosity.WARN to AndroidLog.WARN,
            SdkLogVerbosity.ERROR to AndroidLog.ERROR,
            SdkLogVerbosity.CRITICAL to AndroidLog.ASSERT,
            null to Int.MAX_VALUE
        ).assertExhaustive(SdkLogVerbosity.entries)

        private fun <K, V> Map<K, V>.assertExhaustive(expected: Iterable<K>) =
            apply {
                assertThat(keys).containsAll(expected)
            }

        private fun <K, V> Map<K, V>.assertAllKeysEqualToValuesWhen(keyToValueTransformer: (K) -> V) =
            onEach { (key, value) ->
                assertThat(keyToValueTransformer(key)).isEqualTo(value)
            }
    }
}
