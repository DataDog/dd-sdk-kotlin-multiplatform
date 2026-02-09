/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp

import kotlin.test.Test
import kotlin.test.assertNull

class DatadogTest {

    @Test
    fun `M clear user ID and account ID W stopInstance`() {
        // Given
        val fakeUserId = "user.id"
        val fakeAccountId = "account.id"
        Datadog.currentUserId = fakeUserId
        Datadog.currentAccountId = fakeAccountId

        // When
        Datadog.stopInstance()

        // Then
        assertNull(Datadog.currentUserId)
        assertNull(Datadog.currentAccountId)
    }
}
