/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.internal

import com.datadog.android.api.InternalLogger
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.annotation.StringForgery
import fr.xgouchet.elmyr.junit5.ForgeExtension
import okhttp3.Authenticator
import okhttp3.Challenge
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import java.net.HttpURLConnection
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
class BasicProxyAuthenticatorTest {

    @StringForgery
    lateinit var fakeUsername: String

    @StringForgery
    lateinit var fakePassword: String

    @Mock
    lateinit var mockInternalLogger: InternalLogger

    private var mockRoute: Route? = null

    @Mock
    lateinit var mockResponse: Response

    private lateinit var fakeRequest: Request

    private lateinit var testedAuthenticator: Authenticator

    @BeforeEach
    fun `set up`(forge: Forge) {
        mockRoute = forge.aNullable { mock() }

        fakeRequest = Request.Builder()
            .url("https://${forge.anAlphaNumericalString()}.${forge.anAlphaNumericalString().take(3)}")
            .build()

        whenever(mockResponse.code) doReturn HttpURLConnection.HTTP_PROXY_AUTH
        whenever(mockResponse.challenges()) doReturn forge.shuffle(listOf("Basic", "OkHttp-Preemptive"))
            .take(forge.anInt(min = 1, max = 3))
            .toMutableList()
            .apply { add("UnsupportedScheme") }
            .map { Challenge(scheme = it, realm = "") }
        whenever(mockResponse.request) doReturn fakeRequest

        testedAuthenticator = BasicProxyAuthenticator(fakeUsername, fakePassword) { mockInternalLogger }
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `M return authenticated request W authenticate`() {
        // When
        val updatedRequest = testedAuthenticator.authenticate(mockRoute, mockResponse)

        // Then
        checkNotNull(updatedRequest)
        val expectedAuthenticationValue = Base64.encode("$fakeUsername:$fakePassword".toByteArray())
        assertThat(updatedRequest.header("Proxy-Authorization"))
            .isEqualTo("Basic $expectedAuthenticationValue")
    }

    @Test
    fun `M return null W authenticate { unknown status code }`(
        forge: Forge
    ) {
        // Given
        whenever(mockResponse.code) doReturn forge.anElementFrom(
            forge.anInt(min = 100, max = 407), forge.anInt(min = 408, max = 600)
        )

        // When
        val updatedRequest = testedAuthenticator.authenticate(mockRoute, mockResponse)

        // Then
        assertThat(updatedRequest).isNull()
    }

    @Test
    fun `M return null W authenticate { unknown challenge }`(
        @StringForgery fakeChallengeScheme: String
    ) {
        // Given
        whenever(mockResponse.challenges()) doReturn listOf(Challenge(scheme = fakeChallengeScheme, realm = ""))

        // When
        val updatedRequest = testedAuthenticator.authenticate(mockRoute, mockResponse)

        // Then
        assertThat(updatedRequest).isNull()
    }
}
