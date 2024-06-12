/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.tracking

import com.datadog.kmp.rum.tracking.DefaultUIKitRUMViewsPredicate
import kotlinx.cinterop.BetaInteropApi
import platform.UIKit.UIViewController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultUIKitRUMViewsPredicateTest {

    @OptIn(BetaInteropApi::class)
    @Test
    fun `M call native default UIKit RUM views predicate W createView`() {
        // Given
        val testedInstance = DefaultUIKitRUMViewsPredicate()
        val fakeViewController = NonUIKitViewController()
        val expectedViewName = fakeViewController.`class`().toString()

        // When
        val rumView = testedInstance.createView(fakeViewController)

        // Then
        checkNotNull(rumView)
        assertEquals(expectedViewName, rumView.name)
        assertTrue(rumView.attributes.isEmpty())
    }

    class NonUIKitViewController : UIViewController(nibName = null, bundle = null)
}
