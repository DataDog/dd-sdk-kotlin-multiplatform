/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultSwiftUIRUMViewsPredicateTest {

    @Test
    fun `M call native default Swift RUM views predicate W createView`() {
        // Given
        val testedInstance = DefaultSwiftUIRUMViewsPredicate()
        val fakeSwiftUIComponentName = "fake-swiftui-component"

        // When
        val rumView = testedInstance.createView(fakeSwiftUIComponentName)

        // Then
        checkNotNull(rumView)
        assertEquals(fakeSwiftUIComponentName, rumView.name)
        assertTrue(rumView.attributes.isEmpty())
    }
}
