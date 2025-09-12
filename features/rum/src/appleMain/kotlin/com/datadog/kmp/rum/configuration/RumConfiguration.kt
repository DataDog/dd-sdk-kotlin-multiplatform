/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

import com.datadog.kmp.rum.ExperimentalRumApi
import com.datadog.kmp.rum.configuration.internal.AppleRumConfigurationBuilder
import com.datadog.kmp.rum.tracking.DefaultSwiftUIRUMActionsPredicate
import com.datadog.kmp.rum.tracking.DefaultSwiftUIRUMViewsPredicate
import com.datadog.kmp.rum.tracking.DefaultUIKitRUMViewsPredicate
import com.datadog.kmp.rum.tracking.SwiftUIRUMActionsPredicate
import com.datadog.kmp.rum.tracking.SwiftUIRUMViewsPredicate
import com.datadog.kmp.rum.tracking.UIKitRUMViewsPredicate

/**
 * Enable automatic tracking of `UIViewControllers` as RUM views.
 *
 * RUM will query this predicate for each [platform.UIKit.UIViewController] presented in the app. The predicate
 * implementation should return RUM view parameters if the given controller should start a view, or `null` to ignore it.
 *
 * [DefaultUIKitRUMViewsPredicate] will be used by default, or you can create your own predicate
 * by implementing [UIKitRUMViewsPredicate].
 *
 * Note: Automatic RUM views tracking involves swizzling the [platform.UIKit.UIViewController] lifecycle methods.
 */
fun RumConfiguration.Builder.trackUiKitViews(
    uiKitViewsPredicate: UIKitRUMViewsPredicate = DefaultUIKitRUMViewsPredicate()
): RumConfiguration.Builder {
    nativePlatformBuilder.setUiKitViewsPredicate(uiKitViewsPredicate)
    return this
}

/**
 * Enable automatic tracking of SwiftUI views as RUM views.
 *
 * RUM will query this predicate for each SwiftUI view detected through hosting controllers. The SDK extracts
 * view names from the SwiftUI view hierarchy within those controllers, then passes those names to this predicate to
 * determine which views should be tracked. The predicate implementation should return RUM view parameters if the
 * given view should be tracked, or `null` to ignore it.
 *
 * [DefaultSwiftUIRUMViewsPredicate] will be used by default, or you can create your own predicate
 * by implementing [SwiftUIRUMViewsPredicate].
 *
 * This API is experimental and may change in future. If the result is not good enough, please use manual
 * instrumentation instead.
 *
 * Note: Automatic RUM views tracking involves swizzling the [platform.UIKit.UIViewController] lifecycle methods.
 */
@ExperimentalRumApi
fun RumConfiguration.Builder.trackSwiftUIViews(
    swiftUIViewsPredicate: SwiftUIRUMViewsPredicate = DefaultSwiftUIRUMViewsPredicate()
): RumConfiguration.Builder {
    nativePlatformBuilder.setSwiftUIViewsPredicate(swiftUIViewsPredicate)
    return this
}

/**
 * Enable automatic tracking of SwiftUI view touches as RUM actions.
 *
 * RUM will query this predicate for each view that the user interacts with. The predicate implementation
 * should return RUM action parameters if the given interaction should be accepted, or `null` to ignore it.
 * Touch events on the keyboard are ignored for privacy reasons.
 *
 * [DefaultSwiftUIRUMActionsPredicate] will be used.
 *
 * This API is experimental and may change in future. If the result is not good enough, please use manual
 * instrumentation instead.
 *
 * Note: Automatic RUM action tracking involves swizzling the `UIApplication.sendEvent(_:)` method.
 *
 * @param isLegacyDetectionEnabled Whether to enable SwiftUI action detection on iOS 17 and below. When set to `false`,
 * actions will only be detected on iOS 18+ where the detection is more reliable.
 */
@ExperimentalRumApi
fun RumConfiguration.Builder.trackSwiftUIActions(
    isLegacyDetectionEnabled: Boolean
): RumConfiguration.Builder {
    nativePlatformBuilder.setSwiftUIActionsPredicate(DefaultSwiftUIRUMActionsPredicate(isLegacyDetectionEnabled))
    return this
}

/**
 * Enable automatic tracking of SwiftUI view touches as RUM actions.
 *
 * RUM will query this predicate for each view that the user interacts with. The predicate implementation
 * should return RUM action parameters if the given interaction should be accepted, or `null` to ignore it.
 * Touch events on the keyboard are ignored for privacy reasons.
 *
 * [DefaultSwiftUIRUMActionsPredicate] will be used.
 *
 * This API is experimental and may change in future. If the result is not good enough, please use manual
 * instrumentation instead.
 *
 * Note: Automatic RUM action tracking involves swizzling the `UIApplication.sendEvent(_:)` method.
 */
@ExperimentalRumApi
fun RumConfiguration.Builder.trackSwiftUIActions(
    swiftUIActionsPredicate: SwiftUIRUMActionsPredicate
): RumConfiguration.Builder {
    nativePlatformBuilder.setSwiftUIActionsPredicate(swiftUIActionsPredicate)
    return this
}

/**
 * Enables App Hangs monitoring with the given threshold (in milliseconds).
 *
 * Only App Hangs that last more than this threshold will be reported. The minimal allowed value for this option is
 * 100 milliseconds. To disable hangs monitoring, set this parameter to `null`.
 *
 * Note: Be cautious when setting the threshold to very small values, as it may lead to excessive reporting of hangs.
 *       The SDK implements a secondary thread for monitoring App Hangs. To reduce CPU utilization, it tracks hangs
 *       with a tolerance of 2.5%, meaning that some hangs lasting very close to this threshold may not be reported.
 *
 * Note: App Hangs monitoring requires Datadog Crash Reporting to be enabled. Otherwise stack trace will be
 * not reported in App Hang errors.
 *
 * @param thresholdMs App Hangs threshold in milliseconds. Default is `null` (monitoring is disabled).
 */
fun RumConfiguration.Builder.setAppHangThreshold(thresholdMs: Long?): RumConfiguration.Builder {
    nativePlatformBuilder.setAppHangThreshold(thresholdMs)
    return this
}

/**
 * Determines whether the SDK should track application termination by the watchdog.
 *
 * Read more about watchdog terminations [the Apple official documentation](https://developer.apple.com/documentation/xcode/addressing-watchdog-terminations).
 *
 * @param enabled Enable or disable watchdog terminations tracking. Default: `false`.
 */
fun RumConfiguration.Builder.trackWatchdogTerminations(enabled: Boolean): RumConfiguration.Builder {
    nativePlatformBuilder.trackWatchdogTerminations(enabled)
    return this
}

/**
 * Determines whether the SDK should enable collection of memory warnings.
 *
 * When enabled, all the memory warnings are reported as RUM Errors.
 *
 * @param enabled Enable or disable the collection of memory warnings. Default: `true`.
 */
fun RumConfiguration.Builder.trackMemoryWarnings(enabled: Boolean): RumConfiguration.Builder {
    nativePlatformBuilder.trackMemoryWarnings(enabled)
    return this
}

private val RumConfiguration.Builder.nativePlatformBuilder
    get() = platformBuilder as AppleRumConfigurationBuilder
