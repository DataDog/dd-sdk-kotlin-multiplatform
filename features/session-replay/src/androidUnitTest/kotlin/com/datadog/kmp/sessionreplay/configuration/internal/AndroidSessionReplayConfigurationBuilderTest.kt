/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration.internal

import com.datadog.android.sessionreplay.ExtensionSupport
import com.datadog.android.sessionreplay.SystemRequirementsConfiguration
import com.datadog.kmp.sessionreplay.configuration.ImagePrivacy
import com.datadog.kmp.sessionreplay.configuration.SessionReplayPrivacy
import com.datadog.kmp.sessionreplay.configuration.TextAndInputPrivacy
import com.datadog.kmp.sessionreplay.configuration.TouchPrivacy
import fr.xgouchet.elmyr.annotation.BoolForgery
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
import com.datadog.android.sessionreplay.ImagePrivacy as NativeImagePrivacy
import com.datadog.android.sessionreplay.SessionReplayConfiguration as NativeSessionReplayConfiguration
import com.datadog.android.sessionreplay.SessionReplayPrivacy as NativeSessionReplayPrivacy
import com.datadog.android.sessionreplay.TextAndInputPrivacy as NativeTextAndInputPrivacy
import com.datadog.android.sessionreplay.TouchPrivacy as NativeTouchPrivacy

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
        @Suppress("DEPRECATION")
        verify(mockNativeRumConfigurationBuilder).setPrivacy(fakePrivacy.native)
    }

    @Test
    fun `M call platform configuration builder+setImagePrivacy W setImagePrivacy`(
        @Forgery fakeImagePrivacy: ImagePrivacy
    ) {
        // When
        testedBuilder.setImagePrivacy(fakeImagePrivacy)

        // Then
        verify(mockNativeRumConfigurationBuilder).setImagePrivacy(fakeImagePrivacy.native)
    }

    @Test
    fun `M call platform configuration builder+setTouchPrivacy W setTouchPrivacy`(
        @Forgery fakeTouchPrivacy: TouchPrivacy
    ) {
        // When
        testedBuilder.setTouchPrivacy(fakeTouchPrivacy)

        // Then
        verify(mockNativeRumConfigurationBuilder).setTouchPrivacy(fakeTouchPrivacy.native)
    }

    @Test
    fun `M call platform configuration builder+setTextAndInputPrivacy W setTextAndInputPrivacy`(
        @Forgery fakeTextAndInputPrivacy: TextAndInputPrivacy
    ) {
        // When
        testedBuilder.setTextAndInputPrivacy(fakeTextAndInputPrivacy)

        // Then
        verify(mockNativeRumConfigurationBuilder).setTextAndInputPrivacy(fakeTextAndInputPrivacy.native)
    }

    @Test
    fun `M call platform configuration builder+addExtensionSupport W addExtensionSupport`() {
        // Given
        val mockExtensionSupport = mock<ExtensionSupport>()

        // When
        testedBuilder.addExtensionSupport(mockExtensionSupport)

        // Then
        verify(mockNativeRumConfigurationBuilder).addExtensionSupport(mockExtensionSupport)
    }

    @Test
    fun `M call platform configuration builder+setDynamicOptimizationEnabled W setDynamicOptimizationEnabled`(
        @BoolForgery isDynamicOptimizationsEnabled: Boolean
    ) {
        // When
        testedBuilder.setDynamicOptimizationEnabled(isDynamicOptimizationsEnabled)

        // Then
        verify(mockNativeRumConfigurationBuilder).setDynamicOptimizationEnabled(isDynamicOptimizationsEnabled)
    }

    @Test
    fun `M call platform configuration builder+setSystemRequirements W setSystemRequirements`() {
        // Given
        val mockSystemRequirements = mock<SystemRequirementsConfiguration>()

        // When
        testedBuilder.setSystemRequirements(mockSystemRequirements)

        // Then
        verify(mockNativeRumConfigurationBuilder).setSystemRequirements(mockSystemRequirements)
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

    private val ImagePrivacy.native: com.datadog.android.sessionreplay.ImagePrivacy
        get() = when (this) {
            ImagePrivacy.MASK_NONE -> NativeImagePrivacy.MASK_NONE
            ImagePrivacy.MASK_LARGE_ONLY -> NativeImagePrivacy.MASK_LARGE_ONLY
            ImagePrivacy.MASK_ALL -> NativeImagePrivacy.MASK_ALL
        }

    private val TouchPrivacy.native: com.datadog.android.sessionreplay.TouchPrivacy
        get() = when (this) {
            TouchPrivacy.SHOW -> NativeTouchPrivacy.SHOW
            TouchPrivacy.HIDE -> NativeTouchPrivacy.HIDE
        }

    private val TextAndInputPrivacy.native: com.datadog.android.sessionreplay.TextAndInputPrivacy
        get() = when (this) {
            TextAndInputPrivacy.MASK_SENSITIVE_INPUTS -> NativeTextAndInputPrivacy.MASK_SENSITIVE_INPUTS
            TextAndInputPrivacy.MASK_ALL_INPUTS -> NativeTextAndInputPrivacy.MASK_ALL_INPUTS
            TextAndInputPrivacy.MASK_ALL -> NativeTextAndInputPrivacy.MASK_ALL
        }
}
