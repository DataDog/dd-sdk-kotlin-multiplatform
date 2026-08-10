/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.utils.forge

import com.datadog.kmp.rum.configuration.SlowFramesConfiguration
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.ForgeryFactory

internal class SlowFramesConfigurationForgeryFactory : ForgeryFactory<SlowFramesConfiguration> {
    override fun getForgery(forge: Forge): SlowFramesConfiguration {
        return SlowFramesConfiguration(
            maxSlowFramesAmount = forge.aPositiveInt(strict = true),
            maxSlowFrameThresholdNs = forge.aPositiveLong(strict = true),
            continuousSlowFrameThresholdNs = forge.aPositiveLong(strict = true),
            freezeDurationThresholdNs = forge.aPositiveLong(strict = true),
            minViewLifetimeThresholdNs = forge.aPositiveLong(strict = true)
        )
    }
}
