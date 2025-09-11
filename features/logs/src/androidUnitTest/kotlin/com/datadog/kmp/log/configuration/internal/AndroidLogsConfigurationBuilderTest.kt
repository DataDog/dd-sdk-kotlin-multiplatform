package com.datadog.kmp.log.configuration.internal

import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.log.model.LogEvent
import com.datadog.kmp.log.utils.forge.Configurator
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.annotation.StringForgery
import fr.xgouchet.elmyr.junit5.ForgeConfiguration
import fr.xgouchet.elmyr.junit5.ForgeExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import com.datadog.android.event.EventMapper as NativeEventMapper
import com.datadog.android.log.LogsConfiguration as NativeLogsConfiguration
import com.datadog.android.log.model.LogEvent as NativeLogEvent

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
@ForgeConfiguration(Configurator::class)
class AndroidLogsConfigurationBuilderTest {
    private lateinit var testedBuilder: AndroidLogsConfigurationBuilder

    @Mock
    lateinit var mockNativeRumConfigurationBuilder: NativeLogsConfiguration.Builder

    @BeforeEach
    fun `set up`() {
        testedBuilder = AndroidLogsConfigurationBuilder(mockNativeRumConfigurationBuilder)
    }

    @Test
    fun `M call platform Logs configuration builder+setEventMapper W setEventMapper`(
        @Forgery fakeNativeLogEvent: NativeLogEvent,
        @StringForgery fakeMessage: String,
        @StringForgery fakeTags: String,
        @StringForgery fakeErrorFingerprint: String,
        @StringForgery fakeErrorMessage: String,
        @StringForgery fakeErrorKind: String
    ) {
        // Given
        val fakeEventMapper = EventMapper<LogEvent> {
            it.message = fakeMessage
            it.ddtags = fakeTags

            it.error?.fingerprint = fakeErrorFingerprint
            it.error?.message = fakeErrorMessage
            it.error?.kind = fakeErrorKind

            it
        }

        // When
        testedBuilder.setEventMapper(fakeEventMapper)

        // Then
        argumentCaptor<NativeEventMapper<NativeLogEvent>> {
            verify(mockNativeRumConfigurationBuilder).setEventMapper(capture())
            firstValue.map(fakeNativeLogEvent)

            assertThat(fakeNativeLogEvent.message).isEqualTo(fakeMessage)
            assertThat(fakeNativeLogEvent.ddtags).isEqualTo(fakeTags)

            if (fakeNativeLogEvent.error != null) {
                assertThat(fakeNativeLogEvent.error?.fingerprint).isEqualTo(fakeErrorFingerprint)
                assertThat(fakeNativeLogEvent.error?.kind).isEqualTo(fakeErrorKind)
                assertThat(fakeNativeLogEvent.error?.message).isEqualTo(fakeErrorMessage)
            }
        }
    }

    @Test
    fun `M call platform Logs configuration builder+useCustomEndpoint W useCustomEndpoint`(
        @StringForgery(regex = "https://[a-z]+\\.com(/[a-z]+)+") fakeCustomEndpoint: String
    ) {
        // When
        testedBuilder.useCustomEndpoint(fakeCustomEndpoint)

        // Then
        verify(mockNativeRumConfigurationBuilder).useCustomEndpoint(fakeCustomEndpoint)
    }

    @Test
    fun `M call platform Logs configuration builder+build W build`() {
        // Given
        val mockNativeConfiguration = mock<NativeLogsConfiguration>()
        whenever(mockNativeRumConfigurationBuilder.build()) doReturn mockNativeConfiguration

        // When
        val logsConfiguration = testedBuilder.build()

        // Then
        assertThat(logsConfiguration).isSameAs(mockNativeConfiguration)
    }
}
