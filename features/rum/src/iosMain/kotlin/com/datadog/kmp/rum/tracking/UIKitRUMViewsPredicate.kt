/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import platform.UIKit.UIViewController

/**
 * The predicate deciding if the RUM View should be started or ended for given instance of the [UIViewController].
 */
fun interface UIKitRUMViewsPredicate {
    /**
     * Creates a [RumView] instance from the given instance of the [UIViewController].
     *
     * @param viewController an instance of the view controller noticed by the SDK.
     * @return RUM View parameters if received view controller should start/end the RUM View, `null` otherwise.
     */
    fun createView(viewController: UIViewController): RumView?
}
