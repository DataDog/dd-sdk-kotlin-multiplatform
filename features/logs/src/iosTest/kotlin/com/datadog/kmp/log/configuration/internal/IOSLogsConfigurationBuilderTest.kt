/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.configuration.internal

import com.datadog.kmp.Datadog
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.log.Logger
import com.datadog.kmp.log.Logs
import com.datadog.kmp.log.configuration.LogsConfiguration
import com.datadog.kmp.privacy.TrackingConsent
import com.datadog.tools.concurrent.CountDownLatch
import com.datadog.tools.random.nullable
import com.datadog.tools.random.randomEnumValue
import com.datadog.tools.random.randomThrowable
import kotlin.test.Test
import kotlin.test.assertEquals

class IOSLogsConfigurationBuilderTest {

    @Test
    fun `M call platform Logs configuration builder+setEventMapper W setEventMapper`() {
        // Given
        initializeSdkWithPendingConsent()

        val latch = CountDownLatch(1)

        val logsConfiguration = LogsConfiguration.Builder()
            .setEventMapper {
                latch.countDown()
                it
            }
            .build()
        Logs.enable(logsConfiguration)
        val logger = Logger.Builder()
            .build()

        // When
        logger.log(randomEnumValue(), "fake message", nullable(randomThrowable()))
        latch.await(EVENTS_WAIT_TIMEOUT_MS)
        Datadog.stopInstance()

        // Then
        assertEquals(
            expected = true,
            actual = latch.isExhausted(),
            "Expected user-provided logs event mapper to be called successfully, but it wasn't"
        )
    }

    private fun initializeSdkWithPendingConsent() {
        val fakeConfiguration = Configuration.Builder(
            clientToken = "fakeToken",
            env = "fakeEnv"
        ).build()
        Datadog.initialize(null, fakeConfiguration, TrackingConsent.PENDING)
    }

    private companion object {
        const val EVENTS_WAIT_TIMEOUT_MS = 2000L
    }
}
