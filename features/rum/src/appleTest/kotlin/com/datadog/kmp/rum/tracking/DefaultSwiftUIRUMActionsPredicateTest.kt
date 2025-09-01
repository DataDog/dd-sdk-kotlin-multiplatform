/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import com.datadog.tools.random.randomBoolean
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIUserInterfaceIdiomPhone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultSwiftUIRUMActionsPredicateTest {

    @Test
    fun `M call native default SwiftUI RUM actions predicate W createAction`() {
        // Given
        val fakeLegacyDetectionEnabled = randomBoolean()
        val testedInstance = DefaultSwiftUIRUMActionsPredicate(fakeLegacyDetectionEnabled)
        val fakeSwiftUIComponentName = "fake-swiftui-component"

        // When
        val rumAction = testedInstance.createAction(fakeSwiftUIComponentName)

        // Then
        val majorVersion = NSProcessInfo.processInfo.operatingSystemVersion.useContents { majorVersion }
        val isIOS = UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPhone ||
            UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad
        val isIOS18OrLater = isIOS && majorVersion >= 18

        if (isIOS) {
            if (isIOS18OrLater || fakeLegacyDetectionEnabled) {
                checkNotNull(rumAction)
                assertEquals(fakeSwiftUIComponentName, rumAction.name)
                assertTrue(rumAction.attributes.isEmpty())
            } else {
                assertNull(rumAction)
            }
        } else {
            checkNotNull(rumAction)
            assertEquals(fakeSwiftUIComponentName, rumAction.name)
            assertTrue(rumAction.attributes.isEmpty())
        }
    }
}
