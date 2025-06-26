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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.atLeastOnce
import org.mockito.quality.Strictness
import com.datadog.android.Datadog as DatadogAndroid

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class),
    ExtendWith(TestConfigurationExtension::class)
)
@ForgeConfiguration(Configurator::class)
@MockitoSettings(strictness = Strictness.LENIENT)
internal class DatadogTest {

    private lateinit var datadogAndroidStatic: MockedStatic<DatadogAndroid>

    @BeforeEach
    fun setUp() {
        datadogAndroidStatic = mockStatic(DatadogAndroid::class.java)
    }

    @AfterEach
    fun tearDown() {
        datadogAndroidStatic.close()
    }

    @Test
    fun `M call initialize on implementation instance W initialize`(
        @Forgery configuration: Configuration,
        @Forgery trackingConsent: TrackingConsent
    ) {
        // When
        Datadog.initialize(
            context = appContext.mockInstance,
            configuration = configuration,
            trackingConsent = trackingConsent
        )

        // Then
        datadogAndroidStatic.verify(
            {
                DatadogAndroid.initialize(
                    context = appContext.mockInstance,
                    configuration = configuration.native,
                    trackingConsent = trackingConsent.native
                )
            },
            // this instruction is required here because
            // there will be some other static calls and mockito will fail otherwise
            atLeastOnce()
        )

        assertThat(Datadog.isCrashReportingEnabled).isEqualTo(configuration.coreConfig.trackCrashes)
    }

    @Test
    fun `M throw IllegalArgumentException W initialize { context is null }`(
        @Forgery configuration: Configuration,
        @Forgery trackingConsent: TrackingConsent
    ) {
        // When
        assertThrows<IllegalArgumentException> {
            Datadog.initialize(
                context = null,
                configuration = configuration,
                trackingConsent = trackingConsent
            )
        }
    }

    @Test
    fun `M call isInitialized on implementation instance W isInitialized`() {
        // When
        Datadog.isInitialized()

        // Then
        datadogAndroidStatic.verify { DatadogAndroid.isInitialized() }
    }

    @Test
    fun `M call clearAllData on implementation instance W clearAllData`() {
        // When
        Datadog.clearAllData()

        // Then
        datadogAndroidStatic.verify { DatadogAndroid.clearAllData() }
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
        Datadog.setUserInfo(id, name, email, extraInfo)

        // Then
        datadogAndroidStatic.verify {
            DatadogAndroid.setUserInfo(
                id = id,
                name = name,
                email = email,
                extraInfo = extraInfo
            )
        }
    }

    @Test
    fun `M call addUserProperties on implementation instance W addUserExtraInfo`(
        @MapForgery(
            key = AdvancedForgery(string = [StringForgery(StringForgeryType.ALPHABETICAL)]),
            value = AdvancedForgery(string = [StringForgery(StringForgeryType.ALPHABETICAL)])
        ) extraInfo: Map<String, String>
    ) {
        // When
        Datadog.addUserExtraInfo(extraInfo)

        // Then
        datadogAndroidStatic.verify {
            DatadogAndroid.addUserProperties(
                extraInfo = extraInfo
            )
        }
    }

    @Test
    fun `M call setAccountInfo on implementation instance W setAccountInfo`(
        @StringForgery id: String,
        @StringForgery name: String,
        @MapForgery(
            key = AdvancedForgery(string = [StringForgery(StringForgeryType.ALPHABETICAL)]),
            value = AdvancedForgery(string = [StringForgery(StringForgeryType.ALPHABETICAL)])
        ) extraInfo: Map<String, String>
    ) {
        // When
        Datadog.setAccountInfo(id, name, extraInfo)

        // Then
        datadogAndroidStatic.verify {
            DatadogAndroid.setAccountInfo(
                id = id,
                name = name,
                extraInfo = extraInfo
            )
        }
    }

    @Test
    fun `M call addAccountExtraInfo on implementation instance W addAccountExtraInfo`(
        @MapForgery(
            key = AdvancedForgery(string = [StringForgery(StringForgeryType.ALPHABETICAL)]),
            value = AdvancedForgery(string = [StringForgery(StringForgeryType.ALPHABETICAL)])
        ) extraInfo: Map<String, String>
    ) {
        // When
        Datadog.addAccountExtraInfo(extraInfo)

        // Then
        datadogAndroidStatic.verify {
            DatadogAndroid.addAccountExtraInfo(
                extraInfo = extraInfo
            )
        }
    }

    @Test
    fun `M call clearAccountInfo on implementation instance W clearAccountInfo`() {
        // When
        Datadog.clearAccountInfo()

        // Then
        datadogAndroidStatic.verify {
            DatadogAndroid.clearAccountInfo()
        }
    }

    @Test
    fun `M call setTrackingConsent on implementation instance W setTrackingConsent`(
        @Forgery trackingConsent: TrackingConsent
    ) {
        // When
        Datadog.setTrackingConsent(trackingConsent)

        // Then
        datadogAndroidStatic.verify {
            DatadogAndroid.setTrackingConsent(
                consent = trackingConsent.native
            )
        }
    }

    @Test
    fun `M call stopInstance on implementation instance W stopInstance`() {
        // When
        Datadog.stopInstance()

        // Then
        datadogAndroidStatic.verify { DatadogAndroid.stopInstance() }
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
