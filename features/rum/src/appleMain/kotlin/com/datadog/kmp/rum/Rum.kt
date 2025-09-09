/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum

import cocoapods.DatadogRUM.DDRUM
import cocoapods.DatadogRUM.DDRUMConfiguration
import cocoapods.DatadogRUM.DDRUMErrorSourceSource
import cocoapods.DatadogRUM.DDRUMMonitor
import com.datadog.kmp.internal.INCLUDE_BINARY_IMAGES
import com.datadog.kmp.internal.InternalProxy
import com.datadog.kmp.internal.RUM_ERROR_IS_CRASH
import com.datadog.kmp.internal.addDatadogUnhandledExceptionHook
import com.datadog.kmp.internal.createNSErrorFromThrowable
import com.datadog.kmp.rum.configuration.RumConfiguration

/**
 * An entry point to Datadog RUM feature.
 */
actual object Rum {

    /**
     * Enables a RUM feature based on the configuration provided and registers RUM monitor.
     *
     * @param rumConfiguration Configuration to use for the feature.
     */
    actual fun enable(rumConfiguration: RumConfiguration) {
        DDRUM.enableWith(rumConfiguration.nativeConfiguration as DDRUMConfiguration)

        if (InternalProxy.isCrashReportingEnabled) {
            addDatadogUnhandledExceptionHook {
                DDRUMMonitor.shared()
                    .addErrorWithError(
                        createNSErrorFromThrowable(it),
                        DDRUMErrorSourceSource,
                        mutableMapOf<Any?, Any?>().apply {
                            this += INCLUDE_BINARY_IMAGES to true
                            // we always assume that if this hook is called - then it is a crash. Our own hook
                            // implementation will crash app if it is top-most hook, and if it is not, then it is
                            // responsibility of the hooks upper in the chain. If they don't crash the app - they
                            // should, their fault.
                            this += RUM_ERROR_IS_CRASH to true
                        }
                    )
            }
        }
    }
}
