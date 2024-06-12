/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import platform.UIKit.UIView

/**
 * The predicate deciding if the RUM Action should be recorded.
 */
fun interface UIKitRUMActionsPredicate {
    /**
     * Creates a [RumAction] instance from the given instance of the [UIView].
     *
     * @param targetView an instance of the [UIView] which received the action.
     * @return RUM Action if it should be recorded, `null` otherwise.
     */
    fun createAction(targetView: UIView): RumAction?
}
