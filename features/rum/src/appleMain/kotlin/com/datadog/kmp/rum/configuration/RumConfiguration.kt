/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

import com.datadog.kmp.rum.configuration.internal.AppleRumConfigurationBuilder
import com.datadog.kmp.rum.tracking.DefaultUIKitRUMViewsPredicate
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
 * Note: Automatic RUM views tracking involves swizzling the `UIViewController` lifecycle methods.
 */
fun RumConfiguration.Builder.trackUiKitViews(
    uiKitViewsPredicate: UIKitRUMViewsPredicate = DefaultUIKitRUMViewsPredicate()
): RumConfiguration.Builder {
    nativePlatformBuilder.setUiKitViewsPredicate(uiKitViewsPredicate)
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

private val RumConfiguration.Builder.nativePlatformBuilder
    get() = platformBuilder as AppleRumConfigurationBuilder
