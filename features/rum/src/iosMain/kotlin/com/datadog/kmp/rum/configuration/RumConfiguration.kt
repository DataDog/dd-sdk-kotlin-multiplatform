/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

import com.datadog.kmp.rum.configuration.internal.IOSRumConfigurationBuilder
import com.datadog.kmp.rum.configuration.internal.PlatformRumConfigurationBuilder
import com.datadog.kmp.rum.tracking.DefaultUIKitRUMActionsPredicate
import com.datadog.kmp.rum.tracking.UIKitRUMActionsPredicate

/**
 * Enable automatic tracking of [platform.UIKit.UITouch] events as RUM actions.
 *
 * RUM will query this predicate for each [platform.UIKit.UIView] that the user interacts with. The predicate
 * implementation should return RUM action parameters if the given interaction should be accepted,
 * or `null` to ignore it.
 * Touch events on the keyboard are ignored for privacy reasons.
 *
 * [DefaultUIKitRUMActionsPredicate] will be used by default, or you can create your own predicate by
 * implementing [UIKitRUMActionsPredicate].
 *
 * Note: Automatic RUM action tracking involves swizzling the `UIApplication.sendEvent(_:)` method.
 */
fun RumConfiguration.Builder.trackUiKitActions(
    uiKitActionsPredicate: UIKitRUMActionsPredicate = DefaultUIKitRUMActionsPredicate()
): RumConfiguration.Builder {
    nativePlatformBuilder.setUiKitActionsPredicate(uiKitActionsPredicate)
    return this
}

internal actual fun platformConfigurationBuilder(applicationId: String): PlatformRumConfigurationBuilder<Any> =
    IOSRumConfigurationBuilder(applicationId)

private val RumConfiguration.Builder.nativePlatformBuilder
    get() = platformBuilder as IOSRumConfigurationBuilder
