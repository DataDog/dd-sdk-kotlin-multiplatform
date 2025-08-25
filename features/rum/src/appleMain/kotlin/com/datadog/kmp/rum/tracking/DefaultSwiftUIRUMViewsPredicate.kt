/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import cocoapods.DatadogObjc.DDDefaultSwiftUIRUMViewsPredicate

/**
 * Default implementation of [SwiftUIRUMViewsPredicate].
 * It names RUM Views by component name.
 */
class DefaultSwiftUIRUMViewsPredicate : SwiftUIRUMViewsPredicate {

    internal val nativeDelegate = DDDefaultSwiftUIRUMViewsPredicate()

    /** @inheritdoc */
    override fun createView(componentName: String): RumView? {
        val nativeView = nativeDelegate.rumViewFor(componentName) ?: return null
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
