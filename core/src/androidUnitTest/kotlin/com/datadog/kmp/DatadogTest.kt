/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp

import android.content.Context
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.privacy.TrackingConsent
import com.datadog.kmp.tools.config.ApplicationContextTestConfiguration
import com.datadog.kmp.tools.config.TestConfiguration
import com.datadog.kmp.tools.forge.Configurator
import com.datadog.kmp.tools.unit.annotations.TestConfigurationsProvider
import com.datadog.kmp.tools.unit.extensions.TestConfigurationExtension
import fr.xgouchet.elmyr.annotation.AdvancedForgery
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.annotation.MapForgery
import fr.xgouchet.elmyr.annotation.StringForgery
import fr.xgouchet.elmyr.annotation.StringForgeryType
import fr.xgouchet.elmyr.junit5.ForgeConfiguration
import fr.xgouchet.elmyr.junit5.ForgeExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class),
    ExtendWith(TestConfigurationExtension::class)
)
@ForgeConfiguration(Configurator::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatadogTest {

    private val datadogAndroid = mock<com.datadog.android.Datadog>()

    private lateinit var datadog: Datadog

    @BeforeEach
    fun setUp() {
        datadog = spy(Datadog).also { spy: Datadog ->
            whenever(spy.platformImplementation).doReturn(datadogAndroid)
        }
    }

    @Test
    fun `M call initialize on implementation instance W initialize`(
        @Forgery configuration: Configuration,
        @Forgery trackingConsent: TrackingConsent
    ) {
        // When
        datadog.initialize(
            context = appContext.mockInstance,
            configuration = configuration,
            trackingConsent = trackingConsent
        )

        // Then
        verify(datadog, times(1))
            .initialize(
                context = appContext.mockInstance,
                configuration = configuration,
                trackingConsent = trackingConsent
            )

        assertThat(datadog.isCrashReportingEnabled).isEqualTo(configuration.coreConfig.trackCrashes)
    }

    @Test
    fun `M throw IllegalArgumentException W initialize { context is null}`(
        @Forgery configuration: Configuration,
        @Forgery trackingConsent: TrackingConsent
    ) {
        // When
        assertThrows<IllegalArgumentException> {
            datadog.initialize(
                context = null,
                configuration = configuration,
                trackingConsent = trackingConsent
            )
        }
    }

    @Test
    fun `M call isInitialized on implementation instance W isInitialized`() {
        // When
        datadog.isInitialized()

        // Then
        verify(datadog, times(1))
            .isInitialized()
    }

    @Test
    fun `M call clearAllData on implementation instance W clearAllData`() {
        // When
        datadog.clearAllData()

        // Then
        verify(datadog, times(1))
            .clearAllData()
    }

    @Test
    fun `M call setUserInfo on implementation instance W setUserInfo`(
        @StringForgery id: String,
        @StringForgery name: String,
        @StringForgery email: String,
        @MapForgery(
            key = AdvancedForgery(string = [StringForgery(StringForgeryType.ALPHABETICAL)]),
            value = AdvancedForgery(string = [StringForgery(StringForgeryType.ALPHABETICAL)])
        ) extraInfo: Map<String, String>
    ) {
        // When
        datadog.setUserInfo(id, name, email, extraInfo)

        // Then
        verify(datadog, times(1))
            .setUserInfo(
                id = id,
                name = name,
                email = email,
                extraInfo = extraInfo
            )
    }

    @Test
    fun `M call addUserExtraInfo on implementation instance W addUserExtraInfo`(
        @MapForgery(
            key = AdvancedForgery(string = [StringForgery(StringForgeryType.ALPHABETICAL)]),
            value = AdvancedForgery(string = [StringForgery(StringForgeryType.ALPHABETICAL)])
        ) extraInfo: Map<String, String>
    ) {
        // When
        datadog.addUserExtraInfo(extraInfo)

        // Then
        verify(datadog, times(1))
            .addUserExtraInfo(
                extraInfo = extraInfo
            )
    }

    @Test
    fun `M call setTrackingConsent on implementation instance W setTrackingConsent`(
        @Forgery trackingConsent: TrackingConsent
    ) {
        // When
        datadog.setTrackingConsent(trackingConsent)

        // Then
        verify(datadog, times(1))
            .setTrackingConsent(
                consent = trackingConsent
            )
    }

    @Test
    fun `M call stopInstance on implementation instance W stopInstance`() {
        // When
        datadog.stopInstance()

        // Then
        verify(datadog, times(1))
            .stopInstance()
    }

    companion object {
        private val appContext = ApplicationContextTestConfiguration(Context::class.java)

        @TestConfigurationsProvider
        @JvmStatic
        fun getTestConfigurations(): List<TestConfiguration> {
            return listOf(appContext)
        }
    }
}
