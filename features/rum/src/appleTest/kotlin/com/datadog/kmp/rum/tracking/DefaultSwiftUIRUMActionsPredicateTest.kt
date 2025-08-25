/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import com.datadog.tools.random.randomBoolean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultSwiftUIRUMActionsPredicateTest {

    @Test
    fun `M call native default SwiftUI RUM actions predicate W createView`() {
        // Given
        val testedInstance = DefaultSwiftUIRUMActionsPredicate(randomBoolean())
        val fakeSwiftUIComponentName = "fake-swiftui-component"

        // When
        val rumAction = testedInstance.createAction(fakeSwiftUIComponentName)

        // Then
        checkNotNull(rumAction)
        assertEquals(fakeSwiftUIComponentName, rumAction.name)
        assertTrue(rumAction.attributes.isEmpty())
    }
}
