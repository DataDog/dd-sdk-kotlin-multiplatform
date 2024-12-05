/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.internal

import platform.posix.usleep
import kotlin.concurrent.AtomicReference
import kotlin.experimental.ExperimentalNativeApi

/**
 * Adds a hook with Datadog-specific exception handling by using [setUnhandledExceptionHook].
 *
 * **NOTE**: This is a part of internal API and shouldn't be used outside of the SDK classes.
 *
 * @param action action to perform when unhandled exception happens.
 */
@OptIn(ExperimentalNativeApi::class)
fun addDatadogUnhandledExceptionHook(action: (Throwable) -> Unit) =
    addDatadogUnhandledExceptionHookWithTermination(action) {
        terminateWithUnhandledException(it)
    }

@OptIn(ExperimentalNativeApi::class)
internal fun addDatadogUnhandledExceptionHookWithTermination(
    action: (Throwable) -> Unit,
    terminateAction: (Throwable) -> Unit
) {
    val previousHookHolder = AtomicReference<ReportUnhandledExceptionHook?>(null)
    val hook = object : ReportUnhandledExceptionHook {
        override fun invoke(throwable: Throwable) {
            action.invoke(throwable)
            previousHookHolder.value?.invoke(throwable)
            // hook registration order: A -> B -> C
            // hook processing order: C -> B -> A
            val canCrash = getUnhandledExceptionHook() == this
            if (canCrash) {
                // TODO RUM-5176 RumMonitor.addErrorWithError call is not blocking, so we may have no time to
                //  write a specific error and we will end up with having a generic runtime crash error. Adding
                //  a small sleep call here won't hurt: we get better chances that error is written
                val sleepMicroseconds = 80_000U // 80 ms
                usleep(sleepMicroseconds)

                // our hook is the last one, we can terminate application, otherwise we shift this
                // responsibility to other hooks upper in the chain
                terminateAction(throwable)
            }
        }
    }
    // in the legacy memory model hook should be frozen, but we are going to ask for Kotlin 2.0.20 as minimum
    // version, it is using new memory model
    val previousHook = setUnhandledExceptionHook(hook)
    previousHookHolder.value = previousHook
}
