/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import cocoapods.DatadogObjc.DDDefaultUIKitRUMActionsPredicate
import platform.UIKit.UIView

/**
 * Default implementation of [UIKitRUMActionsPredicate].
 * It names  RUM Actions by the `accessibilityIdentifier` or `className` otherwise.
 */
class DefaultUIKitRUMActionsPredicate : UIKitRUMActionsPredicate {

    internal val nativeDelegate = DDDefaultUIKitRUMActionsPredicate()

    /** @inheritdoc */
    override fun createAction(targetView: UIView): RumAction? {
        val nativeView = nativeDelegate.rumActionWithTargetView(targetView) ?: return null
        return RumAction(
            nativeView.name(),
            // Swift has it like [String,Any], but KMP generates type [Any?,*]
            nativeView.attributes()
                .filterKeys { it is String }
                .mapKeys {
                    it.key as String
                }
        )
    }
}
