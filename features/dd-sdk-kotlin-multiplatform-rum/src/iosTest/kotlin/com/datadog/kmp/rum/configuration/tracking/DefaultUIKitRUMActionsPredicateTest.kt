/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.tracking

import com.datadog.kmp.rum.tracking.DefaultUIKitRUMActionsPredicate
import platform.UIKit.UIView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultUIKitRUMActionsPredicateTest {

    @Test
    fun `M call native default UIKit RUM actions predicate W createAction`() {
        // Given
        val testedInstance = DefaultUIKitRUMActionsPredicate()
        val fakeView = UIView()

        // When
        val rumAction = testedInstance.createAction(fakeView)

        // Then
        checkNotNull(rumAction)
        assertEquals("UIView", rumAction.name)
        assertTrue(rumAction.attributes.isEmpty())
    }
}
