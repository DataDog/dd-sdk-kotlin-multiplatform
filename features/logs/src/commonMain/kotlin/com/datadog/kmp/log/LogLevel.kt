/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log

/**
 * Log levels for the controlling remote logs threshold.
 */
enum class LogLevel {
    /**
     * Debug log level.
     */
    DEBUG,

    /**
     * Info log level.
     */
    INFO,

    /**
     * Warning log level.
     */
    WARN,

    /**
     * Error log level.
     */
    ERROR,

    /**
     * Critical log level.
     */
    CRITICAL
}
