/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
package com.datadog.kmp.tools.config

import fr.xgouchet.elmyr.Forge
import org.mockito.Mockito.mock

open class MockTestConfiguration<T : Any>(
    private val klass: Class<out T>
) : TestConfiguration {

    /**
     * The mocked instance of [T].
     */
    lateinit var mockInstance: T

    /** @inheritdoc */
    override fun setUp(forge: Forge) {
        mockInstance = mock(klass)
    }

    /** @inheritdoc */
    @Suppress("EmptyFunctionBlock")
    override fun tearDown(forge: Forge) {
    }
}
