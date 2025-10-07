/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import cocoapods.DatadogRUM.DDDefaultUIKitRUMActionsPredicate
import cocoapods.DatadogRUM.DDRUMAction
import cocoapods.DatadogRUM.DDRUMConfiguration
import com.datadog.kmp.rum.tracking.DefaultUIKitRUMActionsPredicate
import com.datadog.kmp.rum.tracking.UIKitRUMActionsPredicate
import platform.UIKit.UIPressType
import platform.UIKit.UIView

internal class TvOSRumConfigurationBuilder : AppleRumConfigurationBuilder {

    internal constructor(applicationId: String) : super(applicationId)

    internal constructor(nativeConfiguration: DDRUMConfiguration) : super(nativeConfiguration)

    fun setUiKitActionsPredicate(uiKitActionsPredicate: UIKitRUMActionsPredicate) {
        val nativePredicate = if (uiKitActionsPredicate is DefaultUIKitRUMActionsPredicate) {
            // just a short path to avoid creating unnecessary layers. NB: if DefaultUIKitRUMActionsPredicate becomes
            // open, it is better to remove this branch, because its behavior may become different
            // from the wrapped value
            uiKitActionsPredicate.nativeDelegate
        } else {
            // TODO RUM-4818 by some reason the object which just implements DDUIKitRUMActionsPredicateProtocol cannot
            //  be retrieved once it is successfully set. So extending DDDefaultUIKitRUMActionsPredicate instead (which
            //  implements the same protocol, but on the Swift/ObjC side)
            object : DDDefaultUIKitRUMActionsPredicate() {
                override fun rumActionWithPress(type: UIPressType, targetView: UIView): DDRUMAction? {
                    val rumAction = uiKitActionsPredicate.createAction(type, targetView) ?: return null
                    return DDRUMAction(
                        rumAction.name,
                        rumAction.attributes.mapKeys {
                            @Suppress("USELESS_CAST")
                            it.key as Any
                        }
                    )
                }
            }
        }
        nativeConfiguration.setUiKitActionsPredicate(nativePredicate)
    }
}
