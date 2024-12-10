/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.tools.concurrent

import platform.Foundation.NSDate
import platform.Foundation.now
import platform.Foundation.timeIntervalSinceDate
import kotlin.concurrent.AtomicInt

class CountDownLatch(count: Int) {

    private val count = AtomicInt(count)

    fun countDown() {
        count.decrementAndGet()
    }

    fun await(timeoutMs: Long) {
        val started = NSDate.now
        while (!isExhausted()) {
            val now = NSDate.now
            if (now.timeIntervalSinceDate(started) * MILLISECONDS_IN_SECOND > timeoutMs) {
                break
            }
        }
    }

    fun isExhausted() = count.value <= 0

    private companion object {
        const val MILLISECONDS_IN_SECOND = 1000
    }
}
