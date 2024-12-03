/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import com.datadog.kmp.rum.tracking.RumAction
import com.datadog.kmp.rum.tracking.UIKitRUMActionsPredicate
import com.datadog.tools.random.exhaustiveAttributes
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

internal class TvOSRumConfigurationBuilderTest : AppleRumConfigurationBuilderTest<TvOSRumConfigurationBuilder>() {

    override fun createTestedBuilder() = TvOSRumConfigurationBuilder(fakeNativeRumConfiguration)

    @Test
    fun `M set UIKit actions predicate W setUiKitActionsPredicate`() {
        // Given
        val fakeActionName = "fake-action-name"
        val fakeActionAttributes = exhaustiveAttributes()
        val fakeRumAction = RumAction(fakeActionName, fakeActionAttributes)
        val stubPredicate = UIKitRUMActionsPredicate { _, _ -> fakeRumAction }
        val fakePressType = listOf(
            UIPressTypeMenu,
            UIPressTypeSelect,
            UIPressTypePageUp,
            UIPressTypePageDown,
            UIPressTypeUpArrow,
            UIPressTypeDownArrow,
            UIPressTypeLeftArrow,
            UIPressTypeRightArrow,
            UIPressTypePlayPause
        ).randomElement()

        // When
        testedBuilder.setUiKitActionsPredicate(stubPredicate)

        // Then
        val nativeRumAction = checkNotNull(fakeNativeRumConfiguration.uiKitActionsPredicate())
            .rumActionWithPress(fakePressType, UIView())
        checkNotNull(nativeRumAction)
        assertEquals(fakeActionName, nativeRumAction.name())
        assertEquals(
            expected = fakeActionAttributes,
            actual = nativeRumAction.attributes().mapKeys {
                it.key as String
            }
        )
    }
}
