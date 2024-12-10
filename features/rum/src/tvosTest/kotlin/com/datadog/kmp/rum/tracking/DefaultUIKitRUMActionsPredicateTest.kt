/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import com.datadog.tools.random.randomElement
import platform.UIKit.UIPressTypeDownArrow
import platform.UIKit.UIPressTypeLeftArrow
import platform.UIKit.UIPressTypeMenu
import platform.UIKit.UIPressTypePageDown
import platform.UIKit.UIPressTypePageUp
import platform.UIKit.UIPressTypePlayPause
import platform.UIKit.UIPressTypeRightArrow
import platform.UIKit.UIPressTypeSelect
import platform.UIKit.UIPressTypeUpArrow
import platform.UIKit.UIView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultUIKitRUMActionsPredicateTest {

    @Test
    fun `M call native default UIKit RUM actions predicate W createAction + supported actions `() {
        // Given
        val testedInstance = DefaultUIKitRUMActionsPredicate()
        val fakeView = UIView()
        val (fakePressType, expectedName) = listOf(
            UIPressTypeMenu to "menu",
            UIPressTypeSelect to "UIView",
            UIPressTypePlayPause to "play-pause"
        ).randomElement()

        // When
        val rumAction = testedInstance.createAction(fakePressType, fakeView)

        // Then
        checkNotNull(rumAction)
        assertEquals(expectedName, rumAction.name)
        assertTrue(rumAction.attributes.isEmpty())
    }

    @Test
    fun `M call native default UIKit RUM actions predicate W createAction + unsupported actions `() {
        // Given
        val testedInstance = DefaultUIKitRUMActionsPredicate()
        val fakeView = UIView()
        val fakePressType = listOf(
            UIPressTypePageUp,
            UIPressTypePageDown,
            UIPressTypeUpArrow,
            UIPressTypeDownArrow,
            UIPressTypeLeftArrow,
            UIPressTypeRightArrow
        ).randomElement()

        // When
        val rumAction = testedInstance.createAction(fakePressType, fakeView)

        // Then
        check(rumAction == null, { "RUM Action wasn't null" })
    }
}
