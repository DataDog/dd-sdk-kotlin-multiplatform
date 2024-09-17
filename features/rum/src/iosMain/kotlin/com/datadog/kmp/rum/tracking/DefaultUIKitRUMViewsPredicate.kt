/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import cocoapods.DatadogObjc.DDDefaultUIKitRUMViewsPredicate
import platform.UIKit.UIViewController

/**
 * Default implementation of [UIKitRUMViewsPredicate].
 * It names  RUM Views by the names of their [UIViewController] subclasses.
 */
class DefaultUIKitRUMViewsPredicate : UIKitRUMViewsPredicate {

    internal val nativeDelegate = DDDefaultUIKitRUMViewsPredicate()

    /** @inheritdoc */
    override fun createView(viewController: UIViewController): RumView? {
        val nativeView = nativeDelegate.rumViewFor(viewController) ?: return null
        return RumView(
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
