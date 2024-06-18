/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum

/**
 * Describes the category of a RUM Resource.
 * @see [RumMonitor]
 */
enum class RumResourceKind {
    // Specific kind of JS resources loading
    BEACON,
    FETCH,
    XHR,
    DOCUMENT,

    // Common kinds
    NATIVE,
    IMAGE,
    JS,
    FONT,
    CSS,
    MEDIA,
    OTHER
}
