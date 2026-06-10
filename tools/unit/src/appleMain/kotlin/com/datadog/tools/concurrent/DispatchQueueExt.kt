/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.tools.concurrent

import platform.CoreFoundation.CFRunLoopRunInMode
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicReference

/**
 * iOS SDK requires the task to be in the MainActor executor context if it is on the main thread since iOS SDK 3.12.0.
 *
 * It is not possible to put it there from Kotlin-only code without any Swift bridge, so this method will switch
 * to the background queue to avoid this check.
 */
fun runOnBackgroundQueueAndWait(block: () -> Unit) {
    val failure = AtomicReference<Throwable?>(null)
    val completed = AtomicInt(0)

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
        try {
            block()
        } catch (@Suppress("TooGenericExceptionCaught") t: Throwable) {
            failure.value = t
        } finally {
            completed.value = 1
        }
    }

    while (completed.value == 0) {
        CFRunLoopRunInMode(kCFRunLoopDefaultMode, RUN_LOOP_WAIT_SECONDS, true)
    }
    failure.value?.let { throw it }
}

private const val RUN_LOOP_WAIT_SECONDS = 0.01
