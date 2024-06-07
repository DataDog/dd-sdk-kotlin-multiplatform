/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

import com.datadog.android.rum.RumMonitor
import com.datadog.kmp.rum.configuration.internal.AndroidRumConfigurationBuilder
import com.datadog.kmp.rum.configuration.internal.PlatformRumConfigurationBuilder
import com.datadog.kmp.rum.tracking.InteractionPredicate
import com.datadog.kmp.rum.tracking.ViewAttributesProvider
import com.datadog.kmp.rum.tracking.ViewTrackingStrategy
import com.datadog.kmp.rum.tracking.internal.NoOpInteractionPredicate

/**
 * Enable tracking of non-fatal ANRs. This is enabled by default on Android API 29 and
 * below, and disabled by default on Android API 30 and above. Android API 30+ has a
 * capability to report fatal ANRs (always enabled). Please note, that tracking non-fatal
 * ANRs is using Watchdog thread approach, which can be noisy, and also leads to ANR
 * duplication on Android 30+ if fatal ANR happened, because Watchdog thread approach cannot
 * categorize ANR as fatal or non-fatal.
 *
 * @param enabled whether tracking of non-fatal ANRs is enabled or not.
 */
fun RumConfiguration.Builder.trackNonFatalAnrs(enabled: Boolean): RumConfiguration.Builder {
    nativePlatformBuilder.trackNonFatalAnrs(enabled)
    return this
}

/**
 * Sets the automatic view tracking strategy used by the SDK.
 * By default [com.datadog.kmp.rum.tracking.ActivityViewTrackingStrategy] will be used.
 * @param strategy as the [ViewTrackingStrategy]
 * Note: If `null` is passed, the RUM Monitor will let you handle View events manually.
 * This means that you should call [RumMonitor.startView] and [RumMonitor.stopView]
 * yourself. A view should be started when it becomes visible and interactive
 * (equivalent to `onResume`) and be stopped when it's paused (equivalent to `onPause`).
 * @see [com.datadog.kmp.rum.tracking.ActivityViewTrackingStrategy]
 * @see [com.datadog.kmp.rum.tracking.FragmentViewTrackingStrategy]
 * @see [com.datadog.kmp.rum.tracking.MixedViewTrackingStrategy]
 * @see [com.datadog.kmp.rum.tracking.NavigationViewTrackingStrategy]
 */
fun RumConfiguration.Builder.useViewTrackingStrategy(strategy: ViewTrackingStrategy?): RumConfiguration.Builder {
    nativePlatformBuilder.useViewTrackingStrategy(strategy)
    return this
}

/**
 * Enable the user interaction automatic tracker. By enabling this feature the SDK will intercept
 * UI interaction events (e.g.: taps, scrolls, swipes) and automatically send those as RUM UserActions for you.
 * @param touchTargetExtraAttributesProviders an array with your own implementation of the
 * target attributes provider.
 * @param interactionPredicate an interface to provide custom values for the
 * actions events properties.
 * @see [ViewAttributesProvider]
 * @see [InteractionPredicate]
 */
fun RumConfiguration.Builder.trackUserInteractions(
    touchTargetExtraAttributesProviders: Array<ViewAttributesProvider> = emptyArray(),
    interactionPredicate: InteractionPredicate = NoOpInteractionPredicate()
): RumConfiguration.Builder {
    nativePlatformBuilder
        .trackUserInteractions(touchTargetExtraAttributesProviders, interactionPredicate)
    return this
}

internal actual fun platformConfigurationBuilder(applicationId: String): PlatformRumConfigurationBuilder<Any> =
    AndroidRumConfigurationBuilder(applicationId)

private val RumConfiguration.Builder.nativePlatformBuilder
    get() = platformBuilder as AndroidRumConfigurationBuilder
