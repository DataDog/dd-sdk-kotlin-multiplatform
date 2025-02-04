/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
package com.datadog.kmp.tools.forge

import com.datadog.kmp.DatadogSite
import com.datadog.kmp.core.configuration.BatchProcessingLevel
import com.datadog.kmp.core.configuration.BatchSize
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.core.configuration.ProxyConfiguration
import com.datadog.kmp.core.configuration.ProxyType
import com.datadog.kmp.core.configuration.UploadFrequency
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.ForgeryFactory

internal class ConfigurationForgeryFactory : ForgeryFactory<Configuration> {
    override fun getForgery(forge: Forge) = Configuration(
        coreConfig = Configuration.Core(
            batchSize = forge.aValueFrom(BatchSize::class.java),
            uploadFrequency = forge.aValueFrom(UploadFrequency::class.java),
            site = forge.aValueFrom(DatadogSite::class.java),
            batchProcessingLevel = forge.aValueFrom(BatchProcessingLevel::class.java),
            trackCrashes = forge.aBool(),
            proxyConfiguration = forge.aNullable {
                ProxyConfiguration(
                    type = forge.aValueFrom(ProxyType::class.java),
                    hostname = forge.aString(),
                    port = forge.anInt(min = 0, max = 65535).toUInt()
                )
            },
            backgroundTasksEnabled = forge.aBool()
        ),
        clientToken = forge.aString(),
        env = forge.aStringMatching("[a-zA-Z0-9_:./-]{0,195}[a-zA-Z0-9_./-]"),
        variant = forge.anElementFrom(forge.anAlphabeticalString(), ""),
        service = forge.aStringMatching("[a-z]+(\\.[a-z]+)+")
    )
}
