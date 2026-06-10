/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.internal

import com.datadog.kmp.Datadog

/**
 * This is internal API and shouldn't be used by the clients.
 */
interface DatadogContextProvider {
    /**
     * Current User ID.
     */
    val userId: String?

    /**
     * Current Account ID.
     */
    val accountId: String?

    companion object {
        /**
         * Creates a default instance of [DatadogContextProvider].
         */
        fun get(): DatadogContextProvider = object : DatadogContextProvider {
            override val userId: String?
                get() = Datadog.currentUserId
            override val accountId: String?
                get() = Datadog.currentAccountId
        }
    }
}
