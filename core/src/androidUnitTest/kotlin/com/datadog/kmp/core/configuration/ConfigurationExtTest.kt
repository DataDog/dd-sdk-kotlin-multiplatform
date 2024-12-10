/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.core.configuration

import com.datadog.android.core.configuration.Configuration
import com.datadog.kmp.internal.BasicProxyAuthenticator
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.annotation.BoolForgery
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.annotation.IntForgery
import fr.xgouchet.elmyr.annotation.StringForgery
import fr.xgouchet.elmyr.junit5.ForgeExtension
import okhttp3.Authenticator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.quality.Strictness
import java.net.InetSocketAddress
import java.net.Proxy

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConfigurationExtTest {

    @Test
    fun `M set proxy configuration W setProxy + auth`(
        @Forgery fakeProxyType: ProxyType,
        @IntForgery(min = 0, max = 65_536) fakePort: Int,
        @StringForgery fakeUsername: String,
        @StringForgery fakePassword: String,
        forge: Forge
    ) {
        // Given
        val mockSdkConfiguration = mock<Configuration.Builder>()
        val fakeHostname = forge.aHostname()
        val fakeProxyConfiguration = ProxyConfiguration(
            type = fakeProxyType,
            hostname = fakeHostname,
            port = fakePort.toUInt()
        ).withBasicAuthentication(
            username = fakeUsername,
            password = fakePassword
        )

        // When
        mockSdkConfiguration.setProxy(fakeProxyConfiguration)

        // Then
        val proxyCaptor = argumentCaptor<Proxy>()
        val authenticatorCaptor = argumentCaptor<Authenticator>()
        verify(mockSdkConfiguration)
            .setProxy(proxyCaptor.capture(), authenticatorCaptor.capture())

        with(proxyCaptor.firstValue) {
            val expectedProxyType = if (fakeProxyType == ProxyType.HTTP) Proxy.Type.HTTP else Proxy.Type.SOCKS
            assertThat(type()).isEqualTo(expectedProxyType)
            val address = address()
            check(address is InetSocketAddress)
            assertThat(address.hostName).isEqualTo(fakeHostname)
            assertThat(address.port).isEqualTo(fakePort)
        }

        assertThat(authenticatorCaptor.firstValue).isInstanceOf(BasicProxyAuthenticator::class.java)
    }

    @Test
    fun `M set proxy configuration W setProxy + incomplete auth`(
        @Forgery fakeProxyType: ProxyType,
        @IntForgery(min = 0, max = 65_536) fakePort: Int,
        @BoolForgery isUsernameMissing: Boolean,
        forge: Forge
    ) {
        // Given
        val mockSdkConfiguration = mock<Configuration.Builder>()
        val fakeHostname = forge.aHostname()
        val (fakeUsername, fakePassword) = if (isUsernameMissing) {
            "" to forge.anAlphaNumericalString()
        } else {
            forge.anAlphaNumericalString() to ""
        }
        val fakeProxyConfiguration = ProxyConfiguration(
            type = fakeProxyType,
            hostname = fakeHostname,
            port = fakePort.toUInt()
        ).withBasicAuthentication(
            username = fakeUsername,
            password = fakePassword
        )

        // When
        mockSdkConfiguration.setProxy(fakeProxyConfiguration)

        // Then
        val proxyCaptor = argumentCaptor<Proxy>()
        verify(mockSdkConfiguration)
            .setProxy(proxyCaptor.capture(), isNull())

        with(proxyCaptor.firstValue) {
            val expectedProxyType = if (fakeProxyType == ProxyType.HTTP) Proxy.Type.HTTP else Proxy.Type.SOCKS
            assertThat(type()).isEqualTo(expectedProxyType)
            val address = address()
            check(address is InetSocketAddress)
            assertThat(address.hostName).isEqualTo(fakeHostname)
            assertThat(address.port).isEqualTo(fakePort)
        }
    }

    @Test
    fun `M set proxy configuration W setProxy + no auth`(
        @Forgery fakeProxyType: ProxyType,
        @IntForgery(min = 0, max = 65_536) fakePort: Int,
        forge: Forge
    ) {
        // Given
        val mockSdkConfiguration = mock<Configuration.Builder>()
        val fakeHostname = forge.aHostname()
        val fakeProxyConfiguration = ProxyConfiguration(
            type = fakeProxyType,
            hostname = fakeHostname,
            port = fakePort.toUInt()
        )

        // When
        mockSdkConfiguration.setProxy(fakeProxyConfiguration)

        // Then
        val proxyCaptor = argumentCaptor<Proxy>()
        verify(mockSdkConfiguration)
            .setProxy(proxyCaptor.capture(), isNull())

        with(proxyCaptor.firstValue) {
            val expectedProxyType = if (fakeProxyType == ProxyType.HTTP) Proxy.Type.HTTP else Proxy.Type.SOCKS
            assertThat(type()).isEqualTo(expectedProxyType)
            val address = address()
            check(address is InetSocketAddress)
            assertThat(address.hostName).isEqualTo(fakeHostname)
            assertThat(address.port).isEqualTo(fakePort)
        }
    }

    @Test
    fun `M not set proxy configuration W setProxy + null config`() {
        // Given
        val testSdkConfiguration = mock<Configuration.Builder>()

        // When
        testSdkConfiguration.setProxy(null)

        // Then
        verifyNoInteractions(testSdkConfiguration)
    }

    //region private

    // InetSocketAddress triggers hostname resolution, so we will limit hostname values to the known ones
    private fun Forge.aHostname() = anElementFrom(
        listOf(
            // local name
            "localhost",
            // with domain name
            "datadoghq.com",
            // don't use localhost IPs for below, they will be resolved to "localhost" name
            // IPv4
            "127.0.0.2",
            // IPv6
            "0:0:0:0:0:0:0:2"
        )
    )

    // endregion
}
