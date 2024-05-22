/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log

import com.datadog.kmp.log.internal.IOSPlatformLogger
import platform.Foundation.NSError

/**
 * Sends a Debug log message.
 * @param message the message to be logged
 * @param error an error to be logged with a message
 * @param attributes a map of attributes to include only for this message. If an attribute with
 * the same key already exist in this logger, it will be overridden (just for this message)
 */
fun Logger.debug(message: String, error: NSError, attributes: Map<String, Any?> = emptyMap()) {
    (platformLogger as IOSPlatformLogger).debug(message, error, attributes)
}

/**
 * Sends an Info log message.
 * @param message the message to be logged
 * @param error an error to be logged with a message
 * @param attributes a map of attributes to include only for this message. If an attribute with
 * the same key already exist in this logger, it will be overridden (just for this message)
 */
fun Logger.info(message: String, error: NSError, attributes: Map<String, Any?> = emptyMap()) {
    (platformLogger as IOSPlatformLogger).info(message, error, attributes)
}

/**
 * Sends a Warning log message.
 * @param message the message to be logged
 * @param error an error to be logged with a message
 * @param attributes a map of attributes to include only for this message. If an attribute with
 * the same key already exist in this logger, it will be overridden (just for this message)
 */
fun Logger.warn(message: String, error: NSError, attributes: Map<String, Any?> = emptyMap()) {
    (platformLogger as IOSPlatformLogger).warn(message, error, attributes)
}

/**
 * Sends an Error log message.
 * @param message the message to be logged
 * @param error an error to be logged with a message
 * @param attributes a map of attributes to include only for this message. If an attribute with
 * the same key already exist in this logger, it will be overridden (just for this message)
 */
fun Logger.error(message: String, error: NSError, attributes: Map<String, Any?> = emptyMap()) {
    (platformLogger as IOSPlatformLogger).error(message, error, attributes)
}

/**
 * Sends a Critical log message.
 * @param message the message to be logged
 * @param error an error to be logged with a message
 * @param attributes a map of attributes to include only for this message. If an attribute with
 * the same key already exist in this logger, it will be overridden (just for this message)
 */
fun Logger.critical(message: String, error: NSError, attributes: Map<String, Any?> = emptyMap()) {
    (platformLogger as IOSPlatformLogger).critical(message, error, attributes)
}

/**
 * Sends a log message with a given priority.
 * @param priority the log priority.
 * @param message the message to be logged
 * @param error an error to be logged with a message
 * @param attributes a map of attributes to include only for this message. If an attribute with
 * the same key already exist in this logger, it will be overridden (just for this message)
 */
fun Logger.log(priority: LogLevel, message: String, error: NSError, attributes: Map<String, Any?> = emptyMap()) {
    when (priority) {
        LogLevel.DEBUG -> debug(message, error, attributes)
        LogLevel.INFO -> info(message, error, attributes)
        LogLevel.WARN -> warn(message, error, attributes)
        LogLevel.ERROR -> error(message, error, attributes)
        LogLevel.CRITICAL -> critical(message, error, attributes)
    }
}
