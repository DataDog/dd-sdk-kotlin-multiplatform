/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.core.configuration

import cocoapods.DatadogObjc.DDConfiguration
import com.datadog.tools.random.randomBoolean
import com.datadog.tools.random.randomInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConfigurationExtTest {

    // region proxy

    @Test
    fun `M set proxy configuration W setProxy + HTTP proxy + auth`() {
        // Given
        val testSdkConfiguration = DDConfiguration(clientToken = "fake-token", env = "fake-env")
        val fakeHostname = "some.host"
        val fakePort = randomInt(from = 0, until = 65536).toUInt()
        val fakeUsername = "some-user"
        val fakePassword = "some-pass"
        val fakeProxyConfiguration = ProxyConfiguration(
            type = ProxyType.HTTP,
            hostname = fakeHostname,
            port = fakePort
        ).withBasicAuthentication(
            username = fakeUsername,
            password = fakePassword
        )

        // When
        testSdkConfiguration.setProxy(fakeProxyConfiguration)

        // Then
        assertEquals(
            mapOf(
                "HTTPSEnable" to true,
                "HTTPSPort" to fakePort,
                "HTTPSProxy" to fakeHostname,
                "kCFProxyUsernameKey" to fakeUsername,
                "kCFProxyPasswordKey" to fakePassword
            ),
            testSdkConfiguration.proxyConfiguration()?.mapKeys { it.key as String }
        )
    }

    @Test
    fun `M set proxy configuration W setProxy + HTTP proxy + incomplete auth`() {
        // Given
        val testSdkConfiguration = DDConfiguration(clientToken = "fake-token", env = "fake-env")
        val fakeHostname = "some.host"
        val fakePort = randomInt(from = 0, until = 65536).toUInt()
        val (fakeUsername, fakePassword) = if (randomBoolean()) {
            "some-user" to ""
        } else {
            "" to "some-pass"
        }
        val fakeProxyConfiguration = ProxyConfiguration(
            type = ProxyType.HTTP,
            hostname = fakeHostname,
            port = fakePort
        ).withBasicAuthentication(
            username = fakeUsername,
            password = fakePassword
        )

        // When
        testSdkConfiguration.setProxy(fakeProxyConfiguration)

        // Then
        assertEquals(
            mapOf(
                "HTTPSEnable" to true,
                "HTTPSPort" to fakePort,
                "HTTPSProxy" to fakeHostname
            ),
            testSdkConfiguration.proxyConfiguration()?.mapKeys { it.key as String }
        )
    }

    @Test
    fun `M set proxy configuration W setProxy + HTTP proxy + no auth`() {
        // Given
        val testSdkConfiguration = DDConfiguration(clientToken = "fake-token", env = "fake-env")
        val fakeHostname = "some.host"
        val fakePort = randomInt(from = 0, until = 65536).toUInt()
        val fakeProxyConfiguration = ProxyConfiguration(
            type = ProxyType.HTTP,
            hostname = fakeHostname,
            port = fakePort
        )

        // When
        testSdkConfiguration.setProxy(fakeProxyConfiguration)

        // Then
        assertEquals(
            mapOf(
                "HTTPSEnable" to true,
                "HTTPSPort" to fakePort,
                "HTTPSProxy" to fakeHostname
            ),
            testSdkConfiguration.proxyConfiguration()?.mapKeys { it.key as String }
        )
    }

    @Test
    fun `M set proxy configuration W setProxy + SOCKS proxy + auth`() {
        // Given
        val testSdkConfiguration = DDConfiguration(clientToken = "fake-token", env = "fake-env")
        val fakeHostname = "some.host"
        val fakePort = randomInt(from = 0, until = 65536).toUInt()
        val fakeUsername = "some-user"
        val fakePassword = "some-pass"
        val fakeProxyConfiguration = ProxyConfiguration(
            type = ProxyType.SOCKS,
            hostname = fakeHostname,
            port = fakePort
        ).withBasicAuthentication(
            username = fakeUsername,
            password = fakePassword
        )

        // When
        testSdkConfiguration.setProxy(fakeProxyConfiguration)

        // Then
        assertEquals(
            mapOf(
                "SOCKSEnable" to true,
                "SOCKSPort" to fakePort,
                "SOCKSProxy" to fakeHostname,
                "kCFProxyUsernameKey" to fakeUsername,
                "kCFProxyPasswordKey" to fakePassword
            ),
            testSdkConfiguration.proxyConfiguration()?.mapKeys { it.key as String }
        )
    }

    @Test
    fun `M set proxy configuration W setProxy + SOCKS proxy + incomplete auth`() {
        // Given
        val testSdkConfiguration = DDConfiguration(clientToken = "fake-token", env = "fake-env")
        val fakeHostname = "some.host"
        val fakePort = randomInt(from = 0, until = 65536).toUInt()
        val (fakeUsername, fakePassword) = if (randomBoolean()) {
            "some-user" to ""
        } else {
            "" to "some-pass"
        }
        val fakeProxyConfiguration = ProxyConfiguration(
            type = ProxyType.SOCKS,
            hostname = fakeHostname,
            port = fakePort
        ).withBasicAuthentication(
            username = fakeUsername,
            password = fakePassword
        )

        // When
        testSdkConfiguration.setProxy(fakeProxyConfiguration)

        // Then
        assertEquals(
            mapOf(
                "SOCKSEnable" to true,
                "SOCKSPort" to fakePort,
                "SOCKSProxy" to fakeHostname
            ),
            testSdkConfiguration.proxyConfiguration()?.mapKeys { it.key as String }
        )
    }

    @Test
    fun `M set proxy configuration W setProxy + SOCKS proxy + no auth`() {
        // Given
        val testSdkConfiguration = DDConfiguration(clientToken = "fake-token", env = "fake-env")
        val fakeHostname = "some.host"
        val fakePort = randomInt(from = 0, until = 65536).toUInt()
        val fakeProxyConfiguration = ProxyConfiguration(
            type = ProxyType.SOCKS,
            hostname = fakeHostname,
            port = fakePort
        )

        // When
        testSdkConfiguration.setProxy(fakeProxyConfiguration)

        // Then
        assertEquals(
            mapOf(
                "SOCKSEnable" to true,
                "SOCKSPort" to fakePort,
                "SOCKSProxy" to fakeHostname
            ),
            testSdkConfiguration.proxyConfiguration()?.mapKeys { it.key as String }
        )
    }

    @Test
    fun `M not set proxy configuration W setProxy + null config`() {
        // Given
        val testSdkConfiguration = DDConfiguration(clientToken = "fake-token", env = "fake-env")

        // When
        testSdkConfiguration.setProxy(null)

        // Then
        assertNull(testSdkConfiguration.proxyConfiguration())
    }

    // endregion

    @Test
    fun `M set backgroundTasksEnabled config option W enableBackgroundTasks`() {
        // Given
        val fakeConfigurationBuilder = Configuration.Builder(clientToken = "fake-token", env = "fake-env")
        val enableBackgroundTasks = randomBoolean()

        // When
        fakeConfigurationBuilder.enableBackgroundTasks(enableBackgroundTasks)

        // Then
        assertEquals(enableBackgroundTasks, fakeConfigurationBuilder.coreConfig.backgroundTasksEnabled)
    }
}
