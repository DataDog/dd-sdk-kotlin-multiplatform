/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.internal

import com.datadog.tools.random.randomThrowable
import kotlin.concurrent.AtomicReference
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalNativeApi::class)
class RuntimeExtTest {

    private var defaultUnhandledExceptionHook: ReportUnhandledExceptionHook? = null

    @BeforeTest
    fun `set up`() {
        defaultUnhandledExceptionHook = getUnhandledExceptionHook()
    }

    @AfterTest
    fun `tear down`() {
        setUnhandledExceptionHook(defaultUnhandledExceptionHook)
    }

    @Test
    fun `M crash the app W addDatadogUnhandledExceptionHookWithTermination + our hook is the last one`() {
        // Given
        val datadogHookA = RecordingAction("A")
        val datadogHookB = RecordingAction("B")
        val thirdPartyHook = RecordingAction("ThirdParty")
        val terminateAction = RecordingAction("Terminate")
        addDatadogUnhandledExceptionHookWithTermination(datadogHookA, terminateAction)
        addThirdPartyUnhandledExceptionHook(thirdPartyHook)
        addDatadogUnhandledExceptionHookWithTermination(datadogHookB, terminateAction)
        val fakeThrowable = randomThrowable()

        // When
        getUnhandledExceptionHook()?.invoke(fakeThrowable)

        // Then
        listOf(datadogHookA, datadogHookB, thirdPartyHook, terminateAction).forEach {
            assertTrue(it.invoked, "Expected hook=$it to be called, but it wasn't.")
            assertEquals(1, it.invocationsCount, "Expected hook=$it to be called once, but it wasn't.")
            assertEquals(
                fakeThrowable,
                it.lastInvokedWithThrowable,
                "Expected hook=$it to be called with throwable=$fakeThrowable," +
                    " but it was called with ${it.lastInvokedWithThrowable} instead."
            )
        }
    }

    @Test
    fun `M not crash the app W addDatadogUnhandledExceptionHookWithTermination + our hook is not the last one`() {
        // Given
        val datadogHookA = RecordingAction("A")
        val datadogHookB = RecordingAction("B")
        val thirdPartyHook = RecordingAction("ThirdParty")
        val terminateAction = RecordingAction("Terminate")
        addDatadogUnhandledExceptionHookWithTermination(datadogHookA, terminateAction)
        addDatadogUnhandledExceptionHookWithTermination(datadogHookB, terminateAction)
        addThirdPartyUnhandledExceptionHook(thirdPartyHook)
        val fakeThrowable = randomThrowable()

        // When
        getUnhandledExceptionHook()?.invoke(fakeThrowable)

        // Then
        listOf(datadogHookA, datadogHookB, thirdPartyHook).forEach {
            assertTrue(it.invoked, "Expected hook=$it to be called, but it wasn't.")
            assertEquals(1, it.invocationsCount, "Expected hook=$it to be called once, but it wasn't.")
            assertEquals(
                fakeThrowable,
                it.lastInvokedWithThrowable,
                "Expected hook=$it to be called with throwable=$fakeThrowable," +
                    " but it was called with ${it.lastInvokedWithThrowable} instead."
            )
        }

        assertFalse(terminateAction.invoked, "Expected termination action to not be called, but it was.")
    }

    // region private

    private fun addThirdPartyUnhandledExceptionHook(hook: (Throwable) -> Unit) {
        // imitates some third-party wrap logic where previous hook is respected
        val previousHookReference = AtomicReference<ReportUnhandledExceptionHook?>(null)
        previousHookReference.value = setUnhandledExceptionHook {
            hook.invoke(it)
            previousHookReference.value?.invoke(it)
        }
    }

    private data class RecordingAction(
        val name: String,
        var invoked: Boolean = false,
        var invocationsCount: Int = 0,
        var lastInvokedWithThrowable: Throwable? = null
    ) : (Throwable) -> Unit {

        override fun invoke(throwable: Throwable) {
            invoked = true
            invocationsCount++
            lastInvokedWithThrowable = throwable
        }
    }

    // endregion
}
