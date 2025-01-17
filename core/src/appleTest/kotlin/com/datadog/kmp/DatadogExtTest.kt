/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp

import cocoapods.DatadogObjc.DDBatchProcessingLevelHigh
import cocoapods.DatadogObjc.DDBatchProcessingLevelLow
import cocoapods.DatadogObjc.DDBatchProcessingLevelMedium
import cocoapods.DatadogObjc.DDBatchSizeLarge
import cocoapods.DatadogObjc.DDBatchSizeMedium
import cocoapods.DatadogObjc.DDBatchSizeSmall
import cocoapods.DatadogObjc.DDSDKVerbosityLevelCritical
import cocoapods.DatadogObjc.DDSDKVerbosityLevelDebug
import cocoapods.DatadogObjc.DDSDKVerbosityLevelError
import cocoapods.DatadogObjc.DDSDKVerbosityLevelNone
import cocoapods.DatadogObjc.DDSDKVerbosityLevelWarn
import cocoapods.DatadogObjc.DDUploadFrequencyAverage
import cocoapods.DatadogObjc.DDUploadFrequencyFrequent
import cocoapods.DatadogObjc.DDUploadFrequencyRare
import com.datadog.kmp.core.configuration.BatchProcessingLevel
import com.datadog.kmp.core.configuration.BatchSize
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.core.configuration.ProxyConfiguration
import com.datadog.kmp.core.configuration.ProxyType
import com.datadog.kmp.core.configuration.UploadFrequency
import com.datadog.tools.random.randomBoolean
import com.datadog.tools.random.randomEnumValue
import com.datadog.tools.random.randomUInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("FunctionNaming")
internal class DatadogExtTest {
    // TODO RUM-8122: support TrackingConsent and DatadogSite mappings after isEqual
    //  method is supported for them in iOS code

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
    fun `M return valid native value W UploadFrequency_native`() {
        mapOf(
            UploadFrequency.FREQUENT to DDUploadFrequencyFrequent,
            UploadFrequency.AVERAGE to DDUploadFrequencyAverage,
            UploadFrequency.RARE to DDUploadFrequencyRare
        )
            .assertExhaustive(UploadFrequency.entries)
            .assertAllKeysEqualToValuesWhen { it.native }
    }

    @Test
    fun `M return valid native value W BatchProcessingLevel_native`() {
        mapOf(
            BatchProcessingLevel.HIGH to DDBatchProcessingLevelHigh,
            BatchProcessingLevel.MEDIUM to DDBatchProcessingLevelMedium,
            BatchProcessingLevel.LOW to DDBatchProcessingLevelLow
        )
            .assertExhaustive(BatchProcessingLevel.entries)
            .assertAllKeysEqualToValuesWhen { it.native }
    }

    @Test
    fun `M return valid native value W BatchSize_native`() {
        mapOf(
            BatchSize.LARGE to DDBatchSizeLarge,
            BatchSize.MEDIUM to DDBatchSizeMedium,
            BatchSize.SMALL to DDBatchSizeSmall
        )
            .assertExhaustive(BatchSize.entries)
            .assertAllKeysEqualToValuesWhen { it.native }
    }

    @Test
    fun `M return expected configuration W Configuration_native`() {
        val expectedProxyConfig = mapOf(
            "HTTPSEnable" to true,
            "HTTPSProxy" to "hostname",
            "HTTPSPort" to randomUInt(from = 0u, until = 65535u)
        )

        val commonConfig = Configuration(
            coreConfig = Configuration.Core(
                batchSize = randomEnumValue(),
                uploadFrequency = randomEnumValue(),
                site = randomEnumValue(),
                batchProcessingLevel = randomEnumValue(),
                trackCrashes = randomBoolean(),
                proxyConfiguration = ProxyConfiguration(
                    type = ProxyType.HTTP,
                    hostname = expectedProxyConfig["HTTPSProxy"] as String,
                    port = expectedProxyConfig["HTTPSPort"] as UInt
                ),
                backgroundTasksEnabled = randomBoolean()
            ),
            clientToken = "clientToken",
            env = "env",
            variant = "variant", // this is ignored by ios implementation
            service = "service"

        )

        val nativeConfig = commonConfig.native

        nativeConfig.proxyConfiguration()
            ?.assertAllKeysEqualToValuesWhen { expectedProxyConfig[it] }

        assertEquals(commonConfig.coreConfig.batchSize.native, nativeConfig.batchSize())
        assertEquals(commonConfig.coreConfig.uploadFrequency.native, nativeConfig.uploadFrequency())
        assertEquals(commonConfig.coreConfig.batchProcessingLevel.native, nativeConfig.batchProcessingLevel())
        assertEquals(commonConfig.coreConfig.backgroundTasksEnabled, nativeConfig.backgroundTasksEnabled())
        assertEquals(commonConfig.clientToken, nativeConfig.clientToken())
        assertEquals(commonConfig.env, nativeConfig.env())
        assertEquals(commonConfig.service, nativeConfig.service())
        // TODO RUM-8122: support site equality verification
    }

    companion object {
        private val LOG_VERBOSITY_MAP = mapOf(
            SdkLogVerbosity.DEBUG to DDSDKVerbosityLevelDebug,
            SdkLogVerbosity.WARN to DDSDKVerbosityLevelWarn,
            SdkLogVerbosity.ERROR to DDSDKVerbosityLevelError,
            SdkLogVerbosity.CRITICAL to DDSDKVerbosityLevelCritical,
            null to DDSDKVerbosityLevelNone
        ).assertExhaustive(SdkLogVerbosity.entries)

        private fun <K, V> Map<K, V>.assertExhaustive(expected: Collection<K>) =
            apply {
                assertTrue(keys.containsAll(expected))
            }

        private fun <K, V> Map<K, V>.assertAllKeysEqualToValuesWhen(keyToValueTransformer: (K) -> V) =
            onEach { (key, value) ->
                val expected = keyToValueTransformer(key)
                assertEquals(expected, value)
            }
    }
}
