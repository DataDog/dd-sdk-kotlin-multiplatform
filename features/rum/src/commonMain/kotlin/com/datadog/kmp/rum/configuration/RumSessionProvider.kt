/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

import kotlin.concurrent.Volatile

internal object InternalRumSessionProvider : RumSessionProvider, RumSessionListener {

    @Volatile
    override var sessionId: String? = null
        private set

    override fun onSessionStarted(sessionId: String, isDiscarded: Boolean) {
        this.sessionId = if (isDiscarded) {
            null
        } else {
            sessionId
        }
    }
}

interface RumSessionProvider {

    val sessionId: String?

    companion object {
        /**
         * This is internal API and shouldn't be used by the clients.
         */
        fun get(): RumSessionProvider = InternalRumSessionProvider
    }
}
