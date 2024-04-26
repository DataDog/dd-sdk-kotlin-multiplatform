/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import com.datadog.kmp.Datadog
import com.datadog.kmp.LogLevel
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.privacy.TrackingConsent

@Suppress("MagicNumber")
fun initDatadog(context: Any? = null) {
    Datadog.verbosity = LogLevel.DEBUG

    val configuration = Configuration.Builder(
        clientToken = "foobar",
        env = "prod"
    ).build()

    Datadog.initialize(context = context, configuration = configuration, trackingConsent = TrackingConsent.GRANTED)

    Datadog.setUserInfo(
        name = "Random User",
        email = "user@example.com",
        extraInfo = mapOf("age" to 42, "location" to "universe")
    )
}
