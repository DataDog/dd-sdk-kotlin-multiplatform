/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.utils.forge

import com.datadog.android.rum.internal.domain.event.ResourceTiming
import com.datadog.android.rum.model.ResourceEvent
import com.datadog.tools.unit.forge.exhaustiveAttributes
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.ForgeryFactory
import fr.xgouchet.elmyr.jvm.ext.aTimestamp
import java.net.URL
import java.util.UUID

internal class ResourceEventForgeryFactory :
    ForgeryFactory<ResourceEvent> {
    override fun getForgery(forge: Forge): ResourceEvent {
        val timing = forge.aNullable {
            ResourceTiming(
                dnsStart = forge.aPositiveLong(),
                dnsDuration = forge.aPositiveLong(),
                connectStart = forge.aPositiveLong(),
                connectDuration = forge.aPositiveLong(),
                sslStart = forge.aPositiveLong(),
                sslDuration = forge.aPositiveLong(),
                firstByteStart = forge.aPositiveLong(),
                firstByteDuration = forge.aPositiveLong(),
                downloadStart = forge.aPositiveLong(),
                downloadDuration = forge.aPositiveLong()
            )
        }
        return ResourceEvent(
            date = forge.aTimestamp(),
            resource = ResourceEvent.Resource(
                id = forge.aNullable { getForgery<UUID>().toString() },
                type = forge.getForgery(),
                url = forge.aStringMatching("https://[a-z]+.[a-z]{3}/[a-z0-9_/]+"),
                duration = forge.aNullable { aPositiveLong() },
                method = forge.aNullable(),
                statusCode = forge.aNullable { aLong(200, 600) },
                size = forge.aNullable { aPositiveLong() },
                dns = timing?.let {
                    if (it.dnsStart > 0) {
                        ResourceEvent.Dns(duration = it.dnsDuration, start = it.dnsStart)
                    } else {
                        null
                    }
                },
                connect = timing?.let {
                    if (it.connectStart > 0) {
                        ResourceEvent.Connect(duration = it.connectDuration, start = it.connectStart)
                    } else {
                        null
                    }
                },
                ssl = timing?.let {
                    if (it.sslStart > 0) {
                        ResourceEvent.Ssl(duration = it.sslDuration, start = it.sslStart)
                    } else {
                        null
                    }
                },
                firstByte = timing?.let {
                    if (it.firstByteStart > 0) {
                        ResourceEvent.FirstByte(duration = it.firstByteDuration, start = it.firstByteStart)
                    } else {
                        null
                    }
                },
                download = timing?.let {
                    if (it.downloadStart > 0) {
                        ResourceEvent.Download(duration = it.downloadDuration, start = it.downloadStart)
                    } else {
                        null
                    }
                },
                redirect = forge.aNullable {
                    ResourceEvent.Redirect(
                        aPositiveLong(),
                        aPositiveLong()
                    )
                },
                provider = forge.aNullable {
                    ResourceEvent.Provider(
                        domain = aNullable { aStringMatching("[a-z]+\\.[a-z]{3}") },
                        name = aNullable { anAlphabeticalString() },
                        type = aNullable()
                    )
                },
                graphql = forge.aNullable {
                    ResourceEvent.Graphql(
                        operationType = aValueFrom(ResourceEvent.OperationType::class.java),
                        operationName = aNullable { aString() },
                        payload = aNullable { aString() },
                        variables = aNullable { aString() }
                    )
                }
            ),
            view = ResourceEvent.ResourceEventView(
                id = forge.getForgery<UUID>().toString(),
                url = forge.aStringMatching("https://[a-z]+.[a-z]{3}/[a-z0-9_/]+"),
                referrer = forge.aNullable { getForgery<URL>().toString() },
                name = forge.aNullable { anAlphabeticalString() }
            ),
            connectivity = forge.aNullable {
                ResourceEvent.Connectivity(
                    status = getForgery(),
                    interfaces = aList { getForgery() },
                    cellular = aNullable {
                        ResourceEvent.Cellular(
                            technology = aNullable { anAlphabeticalString() },
                            carrierName = aNullable { anAlphabeticalString() }
                        )
                    }
                )
            },
            synthetics = forge.aNullable {
                ResourceEvent.Synthetics(
                    testId = forge.anHexadecimalString(),
                    resultId = forge.anHexadecimalString()
                )
            },
            usr = forge.aNullable {
                ResourceEvent.Usr(
                    id = aNullable { anHexadecimalString() },
                    name = aNullable { aStringMatching("[A-Z][a-z]+ [A-Z]\\. [A-Z][a-z]+") },
                    email = aNullable { aStringMatching("[a-z]+\\.[a-z]+@[a-z]+\\.[a-z]{3}") },
                    additionalProperties = exhaustiveAttributes(excludedKeys = setOf("id", "name", "email"))
                )
            },
            account = forge.aNullable {
                ResourceEvent.Account(
                    id = anHexadecimalString(),
                    name = aNullable { aStringMatching("[A-Z][a-z]+ [A-Z]\\. [A-Z][a-z]+") },
                    additionalProperties = exhaustiveAttributes(excludedKeys = setOf("id", "name"))
                )
            },
            action = forge.aNullable {
                ResourceEvent.Action(aList { getForgery<UUID>().toString() })
            },
            application = ResourceEvent.Application(forge.getForgery<UUID>().toString()),
            service = forge.aNullable { anAlphabeticalString() },
            session = ResourceEvent.ResourceEventSession(
                id = forge.getForgery<UUID>().toString(),
                type = ResourceEvent.ResourceEventSessionType.USER,
                hasReplay = forge.aNullable { aBool() }
            ),
            source = forge.aNullable { aValueFrom(ResourceEvent.ResourceEventSource::class.java) },
            ciTest = forge.aNullable {
                ResourceEvent.CiTest(anHexadecimalString())
            },
            os = forge.aNullable {
                ResourceEvent.Os(
                    name = forge.aString(),
                    version = "${forge.aSmallInt()}.${forge.aSmallInt()}.${forge.aSmallInt()}",
                    versionMajor = forge.aSmallInt().toString()
                )
            },
            device = forge.aNullable {
                ResourceEvent.Device(
                    name = forge.aString(),
                    model = forge.aString(),
                    brand = forge.aString(),
                    type = forge.aValueFrom(ResourceEvent.DeviceType::class.java),
                    architecture = forge.aString()
                )
            },
            context = forge.aNullable {
                ResourceEvent.Context(
                    additionalProperties = forge.exhaustiveAttributes()
                )
            },
            dd = ResourceEvent.Dd(
                session = forge.aNullable { ResourceEvent.DdSession(aNullable { getForgery() }) },
                browserSdkVersion = forge.aNullable { aStringMatching("\\d+\\.\\d+\\.\\d+") },
                spanId = forge.aNullable { aNumericalString() },
                traceId = forge.aNullable { aNumericalString() }
            )
        )
    }
}
