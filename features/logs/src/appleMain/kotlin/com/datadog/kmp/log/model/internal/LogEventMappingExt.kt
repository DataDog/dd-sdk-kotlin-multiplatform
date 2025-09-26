/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.model.internal

import cocoapods.DatadogLogs.DDLogEvent
import cocoapods.DatadogLogs.DDLogEventAccountInfo
import cocoapods.DatadogLogs.DDLogEventDDDevice
import cocoapods.DatadogLogs.DDLogEventDd
import cocoapods.DatadogLogs.DDLogEventDevice
import cocoapods.DatadogLogs.DDLogEventDeviceDeviceType
import cocoapods.DatadogLogs.DDLogEventDeviceDeviceTypeBot
import cocoapods.DatadogLogs.DDLogEventDeviceDeviceTypeDesktop
import cocoapods.DatadogLogs.DDLogEventDeviceDeviceTypeGamingConsole
import cocoapods.DatadogLogs.DDLogEventDeviceDeviceTypeMobile
import cocoapods.DatadogLogs.DDLogEventDeviceDeviceTypeOther
import cocoapods.DatadogLogs.DDLogEventDeviceDeviceTypeTablet
import cocoapods.DatadogLogs.DDLogEventDeviceDeviceTypeTv
import cocoapods.DatadogLogs.DDLogEventError
import cocoapods.DatadogLogs.DDLogEventOperatingSystem
import cocoapods.DatadogLogs.DDLogEventStatus
import cocoapods.DatadogLogs.DDLogEventStatusCritical
import cocoapods.DatadogLogs.DDLogEventStatusDebug
import cocoapods.DatadogLogs.DDLogEventStatusEmergency
import cocoapods.DatadogLogs.DDLogEventStatusError
import cocoapods.DatadogLogs.DDLogEventStatusInfo
import cocoapods.DatadogLogs.DDLogEventStatusWarn
import cocoapods.DatadogLogs.DDLogEventUserInfo
import com.datadog.kmp.log.model.LogEvent
import platform.Foundation.NSISO8601DateFormatter

private val ISO_8601_DATE_FORMATTER = NSISO8601DateFormatter()

internal fun DDLogEvent.toCommonModel(): LogEvent = LogEvent(
    os = os().toCommonModel(),
    device = device().toCommonModel(),
    status = logEventStatusToCommonEnum(status()),
    service = serviceName(),
    message = message(),
    date = ISO_8601_DATE_FORMATTER.stringFromDate(date()),
    logger = LogEvent.Logger(
        name = loggerName(),
        version = loggerVersion()
    ),
    dd = dd().toCommonModel(),
    usr = userInfo().toCommonModel(),
    account = accountInfo()?.toCommonModel(),
    // TODO RUM-6098 The way network/carrier information is passed varies a lot between Android and iOS, removing it
    //  from the model for now
    error = error()?.toCommonModel(),
    buildId = buildId(),
    ddtags = tags()?.joinToString(",") { it as String }.orEmpty(),
    additionalProperties = attributes()
        .userAttributes()
        .mapKeys { it.key as String }
        .toMutableMap()
)

internal fun DDLogEventOperatingSystem.toCommonModel(): LogEvent.Os = LogEvent.Os(
    name = name(),
    version = version(),
    build = build(),
    versionMajor = versionMajor()
)

internal fun logEventStatusToCommonEnum(enumValue: DDLogEventStatus): LogEvent.Status =
    when (enumValue) {
        DDLogEventStatusCritical -> LogEvent.Status.CRITICAL
        DDLogEventStatusError -> LogEvent.Status.ERROR
        DDLogEventStatusWarn -> LogEvent.Status.WARN
        DDLogEventStatusInfo -> LogEvent.Status.INFO
        DDLogEventStatusDebug -> LogEvent.Status.DEBUG
        DDLogEventStatusEmergency -> LogEvent.Status.EMERGENCY
        else -> LogEvent.Status.INFO
    }

internal fun logEventDeviceTypeToCommonEnum(enumValue: DDLogEventDeviceDeviceType): LogEvent.Type =
    when (enumValue) {
        DDLogEventDeviceDeviceTypeMobile -> LogEvent.Type.MOBILE
        DDLogEventDeviceDeviceTypeTablet -> LogEvent.Type.TABLET
        DDLogEventDeviceDeviceTypeTv -> LogEvent.Type.TV
        DDLogEventDeviceDeviceTypeGamingConsole -> LogEvent.Type.GAMING_CONSOLE
        DDLogEventDeviceDeviceTypeBot -> LogEvent.Type.BOT
        DDLogEventDeviceDeviceTypeDesktop -> LogEvent.Type.DESKTOP
        DDLogEventDeviceDeviceTypeOther -> LogEvent.Type.OTHER
        else -> LogEvent.Type.MOBILE
    }

internal fun DDLogEventDd.toCommonModel(): LogEvent.Dd = LogEvent.Dd(
    device = device().toCommonModel()
)

internal fun DDLogEventDevice.toCommonModel(): LogEvent.LogEventDevice = LogEvent.LogEventDevice(
    architecture = architecture(),
    type = logEventDeviceTypeToCommonEnum(type()),
    name = name(),
    model = model(),
    brand = brand()
)

internal fun DDLogEventDDDevice.toCommonModel(): LogEvent.DdDevice = LogEvent.DdDevice(
    architecture = architecture()
)

internal fun DDLogEventUserInfo.toCommonModel(): LogEvent.Usr = LogEvent.Usr(
    id = id(),
    name = name(),
    email = email(),
    additionalProperties = extraInfo().mapKeys { it.key as String }.toMutableMap()
)

internal fun DDLogEventAccountInfo.toCommonModel(): LogEvent.Account = LogEvent.Account(
    id = id(),
    name = name(),
    additionalProperties = extraInfo().mapKeys { it.key as String }.toMutableMap()
)

internal fun DDLogEventError.toCommonModel(): LogEvent.Error = LogEvent.Error(
    kind = kind(),
    message = message(),
    stack = stack(),
    sourceType = sourceType(),
    fingerprint = fingerprint()
)
