/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import com.datadog.kmp.rum.configuration.RumSessionListener

internal class CombinedRumSessionListener(
    private val internalListener: RumSessionListener,
    internal val userListener: RumSessionListener?
) : RumSessionListener {
    override fun onSessionStarted(sessionId: String, isDiscarded: Boolean) {
        internalListener.onSessionStarted(sessionId, isDiscarded)
        userListener?.onSessionStarted(sessionId, isDiscarded)
    }
}
