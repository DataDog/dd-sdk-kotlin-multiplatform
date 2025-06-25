/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.model.internal

import cocoapods.DatadogObjc.DDLogEvent
import cocoapods.DatadogObjc.DDLogEventDd
import cocoapods.DatadogObjc.DDLogEventDeviceInfo
import cocoapods.DatadogObjc.DDLogEventError
import cocoapods.DatadogObjc.DDLogEventStatus
import cocoapods.DatadogObjc.DDLogEventStatusCritical
import cocoapods.DatadogObjc.DDLogEventStatusDebug
import cocoapods.DatadogObjc.DDLogEventStatusEmergency
import cocoapods.DatadogObjc.DDLogEventStatusError
import cocoapods.DatadogObjc.DDLogEventStatusInfo
import cocoapods.DatadogObjc.DDLogEventStatusWarn
import cocoapods.DatadogObjc.DDLogEventUserInfo
import com.datadog.kmp.log.model.LogEvent
import platform.Foundation.NSISO8601DateFormatter

private val ISO_8601_DATE_FORMATTER = NSISO8601DateFormatter()

internal fun DDLogEvent.toCommonModel(): LogEvent = LogEvent(
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
    // TODO RUM-10485 LogEvent.account is missing in iOS SDK ObjC API
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

internal fun DDLogEventDd.toCommonModel(): LogEvent.Dd = LogEvent.Dd(
    device = device().toCommonModel()
)

internal fun DDLogEventDeviceInfo.toCommonModel(): LogEvent.Device = LogEvent.Device(
    architecture = architecture()
)

internal fun DDLogEventUserInfo.toCommonModel(): LogEvent.Usr = LogEvent.Usr(
    id = id(),
    name = name(),
    email = email(),
    additionalProperties = extraInfo().mapKeys { it.key as String }.toMutableMap()
)

internal fun DDLogEventError.toCommonModel(): LogEvent.Error = LogEvent.Error(
    kind = kind(),
    message = message(),
    stack = stack(),
    sourceType = sourceType(),
    fingerprint = fingerprint()
)
