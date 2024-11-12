/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import com.datadog.kmp.Datadog
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.log.Logs
import com.datadog.kmp.log.configuration.LogsConfiguration
import com.datadog.kmp.privacy.TrackingConsent
import kotlin.test.Test

class SimpleTest {

    @Test
    fun simpleTest() {
        Datadog.initialize(
            null,
            Configuration.Builder("fake-token", "fake-env")
                .build(),
            TrackingConsent.PENDING
        )

        val logsConfiguration = LogsConfiguration.Builder().build()
        Logs.enable(logsConfiguration)
    }
}
