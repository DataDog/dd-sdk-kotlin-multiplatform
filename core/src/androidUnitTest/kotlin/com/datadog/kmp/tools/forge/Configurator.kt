/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
package com.datadog.kmp.tools.forge

import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.ForgeConfigurator
import fr.xgouchet.elmyr.jvm.useJvmFactories

internal class Configurator : ForgeConfigurator {
    override fun configure(forge: Forge) {
        // Primitives
        forge.useJvmFactories()

        // Core
        forge.addFactory(ConfigurationForgeryFactory())
    }
}
