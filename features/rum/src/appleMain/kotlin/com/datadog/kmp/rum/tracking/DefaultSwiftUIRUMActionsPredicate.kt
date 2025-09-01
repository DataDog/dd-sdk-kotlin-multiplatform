/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import cocoapods.DatadogObjc.DDDefaultSwiftUIRUMActionsPredicate

/**
 * Default implementation of [SwiftUIRUMActionsPredicate].
 * It names RUM Actions by the name of the SwiftUI component that received the action.
 */
class DefaultSwiftUIRUMActionsPredicate(isLegacyDetectionEnabled: Boolean) : SwiftUIRUMActionsPredicate {

    internal val nativeDelegate = DDDefaultSwiftUIRUMActionsPredicate(isLegacyDetectionEnabled)

    /** @inheritdoc */
    override fun createAction(componentName: String): RumAction? {
        val nativeAction = nativeDelegate.rumActionWith(componentName) ?: return null
        return RumAction(
            nativeAction.name(),
            // Swift has it like [String,Any], but KMP generates type [Any?,*]
            nativeAction.attributes()
                .filterKeys { it is String }
                .mapKeys {
                    it.key as String
                }
        )
    }
}
