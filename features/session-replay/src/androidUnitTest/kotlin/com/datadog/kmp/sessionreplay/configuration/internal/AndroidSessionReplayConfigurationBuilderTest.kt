/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration.internal

import com.datadog.kmp.sessionreplay.configuration.SessionReplayPrivacy
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.junit5.ForgeExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import com.datadog.android.sessionreplay.SessionReplayConfiguration as NativeSessionReplayConfiguration
import com.datadog.android.sessionreplay.SessionReplayPrivacy as NativeSessionReplayPrivacy

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
class AndroidSessionReplayConfigurationBuilderTest {

    private lateinit var testedBuilder: AndroidSessionReplayConfigurationBuilder

    @Mock
    lateinit var mockNativeRumConfigurationBuilder: NativeSessionReplayConfiguration.Builder

    @BeforeEach
    fun `set up`() {
        testedBuilder = AndroidSessionReplayConfigurationBuilder(mockNativeRumConfigurationBuilder)
    }

    @Test
    fun `M call platform configuration builder+setPrivacy W setPrivacy`(
        @Forgery fakePrivacy: SessionReplayPrivacy
    ) {
        // When
        testedBuilder.setPrivacy(fakePrivacy)

        // Then
        verify(mockNativeRumConfigurationBuilder).setPrivacy(fakePrivacy.native)
    }

    @Test
    fun `M call platform configuration builder+build W build`() {
        // Given
        val mockNativeConfiguration = mock<NativeSessionReplayConfiguration>()
        whenever(mockNativeRumConfigurationBuilder.build()) doReturn mockNativeConfiguration

        // When
        val sessionReplayConfiguration = testedBuilder.build()

        // Then
        assertThat(sessionReplayConfiguration).isSameAs(mockNativeConfiguration)
    }

    private val SessionReplayPrivacy.native: NativeSessionReplayPrivacy
        get() = when (this) {
            SessionReplayPrivacy.MASK -> NativeSessionReplayPrivacy.MASK
            SessionReplayPrivacy.MASK_USER_INPUT -> NativeSessionReplayPrivacy.MASK_USER_INPUT
            SessionReplayPrivacy.ALLOW -> NativeSessionReplayPrivacy.ALLOW
        }
}
