/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import com.datadog.kmp.rum.tracking.RumAction
import com.datadog.kmp.rum.tracking.UIKitRUMActionsPredicate
import com.datadog.tools.random.exhaustiveAttributes
import platform.UIKit.UIView
import kotlin.test.Test
import kotlin.test.assertEquals

internal class IOSRumConfigurationBuilderTest : AppleRumConfigurationBuilderTest<IOSRumConfigurationBuilder>() {

    override fun createTestedBuilder() = IOSRumConfigurationBuilder(fakeNativeRumConfiguration)

    @Test
    fun `M set UIKit actions predicate W setUiKitActionsPredicate`() {
        // Given
        val fakeActionName = "fake-action-name"
        val fakeActionAttributes = exhaustiveAttributes()
        val fakeRumAction = RumAction(fakeActionName, fakeActionAttributes)
        val stubPredicate = UIKitRUMActionsPredicate { fakeRumAction }

        // When
        testedBuilder.setUiKitActionsPredicate(stubPredicate)

        // Then
        val nativeRumAction = checkNotNull(fakeNativeRumConfiguration.uiKitActionsPredicate())
            .rumActionWithTargetView(UIView())
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
