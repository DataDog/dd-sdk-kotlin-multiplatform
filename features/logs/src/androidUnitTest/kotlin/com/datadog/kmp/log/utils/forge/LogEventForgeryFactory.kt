/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.utils.forge

import com.datadog.android.api.context.NetworkInfo
import com.datadog.android.log.model.LogEvent
import com.datadog.tools.unit.forge.aThrowable
import com.datadog.tools.unit.forge.exhaustiveAttributes
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.ForgeryFactory
import fr.xgouchet.elmyr.jvm.ext.aTimestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

internal class LogEventForgeryFactory : ForgeryFactory<LogEvent> {
    override fun getForgery(forge: Forge): LogEvent {
        val networkInfo = forge.aNullable {
            NetworkInfo(
                connectivity = forge.aValueFrom(NetworkInfo.Connectivity::class.java),
                carrierName = forge.anElementFrom(
                    forge.anAlphabeticalString(),
                    forge.aWhitespaceString(),
                    null
                ),
                carrierId = forge.aNullable { forge.aLong(0, 10000) },
                upKbps = forge.aNullable { forge.aLong(1, Long.MAX_VALUE) },
                downKbps = forge.aNullable { forge.aLong(1, Long.MAX_VALUE) },
                strength = forge.aNullable { forge.aLong(-100, -30) }, // dBm for wifi signal
                cellularTechnology = forge.aNullable { anAlphabeticalString() }
            )
        }
        val reservedKeysAsSet = mutableSetOf<String>().apply {
            arrayOf(
                "status", "service", "message",
                "date", "logger", "_dd", "usr", "network", "error", "build_id", "ddtags"
            ).forEach {
                this.add(it)
            }
        }

        return LogEvent(
            service = forge.anAlphabeticalString(),
            status = forge.aValueFrom(LogEvent.Status::class.java),
            message = forge.anAlphabeticalString(),
            date = forge.aFormattedTimestamp(),
            buildId = forge.aNullable { getForgery<UUID>().toString() },
            error = forge.aNullable {
                val throwable = forge.aNullable { aThrowable() }
                LogEvent.Error(
                    message = throwable?.message,
                    stack = throwable?.stackTraceToString(),
                    kind = throwable?.javaClass?.canonicalName ?: throwable?.javaClass?.simpleName,
                    threads = aNullable {
                        aList {
                            LogEvent.Thread(
                                name = anAlphaNumericalString(),
                                crashed = aBool(),
                                stack = aThrowable().stackTraceToString(),
                                state = aNullable { getForgery<Thread.State>().name.lowercase() }
                            )
                        }
                    }
                )
            },
            additionalProperties = forge.exhaustiveAttributes(
                excludedKeys = reservedKeysAsSet,
                filterThreshold = 0f
            ),
            ddtags = forge.exhaustiveTags().joinToString(separator = ","),
            usr = forge.aNullable {
                LogEvent.Usr(
                    id = forge.aNullable { anHexadecimalString() },
                    name = forge.aNullable { forge.aStringMatching("[A-Z][a-z]+ [A-Z]\\. [A-Z][a-z]+") },
                    email = forge.aNullable { forge.aStringMatching("[a-z]+\\.[a-z]+@[a-z]+\\.[a-z]{3}") },
                    additionalProperties = forge.exhaustiveAttributes(excludedKeys = setOf("id", "name", "email"))
                )
            },
            account = forge.aNullable {
                LogEvent.Account(
                    id = anHexadecimalString(),
                    name = forge.aNullable { forge.aStringMatching("[A-Z][a-z]+ [A-Z]\\. [A-Z][a-z]+") },
                    additionalProperties = forge.exhaustiveAttributes(excludedKeys = setOf("id", "name", "email"))
                )
            },
            network = forge.aNullable {
                LogEvent.Network(
                    client = LogEvent.Client(
                        simCarrier = forge.aNullable {
                            LogEvent.SimCarrier(
                                id = networkInfo?.carrierId?.toString(),
                                name = networkInfo?.carrierName
                            )
                        },
                        signalStrength = networkInfo?.strength?.toString(),
                        uplinkKbps = networkInfo?.upKbps?.toString(),
                        downlinkKbps = networkInfo?.downKbps?.toString(),
                        connectivity = networkInfo?.connectivity?.toString().orEmpty()
                    )
                )
            },
            logger = LogEvent.Logger(
                name = forge.anAlphabeticalString(),
                version = forge.aStringMatching("[0-9]\\.[0-9]\\.[0-9]"),
                threadName = forge.aNullable { forge.anAlphabeticalString() }
            ),
            dd = LogEvent.Dd(
                device = LogEvent.Device(
                    architecture = forge.anAlphaNumericalString()
                )
            )
        )
    }

    private fun Forge.exhaustiveTags(): List<String> {
        return aList { aStringMatching("[a-z]([a-z0-9_:./-]{0,198}[a-z0-9_./-])?") }
    }

    private fun Forge.aFormattedTimestamp(format: String = ISO_8601): String {
        val simpleDateFormat = SimpleDateFormat(format, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return simpleDateFormat.format(this.aTimestamp())
    }

    companion object {
        internal const val ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }
}
