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
    /**
     * Beacon type resource.
     */
    BEACON,

    /**
     * Fetch type resource.
     */
    FETCH,

    /**
     * XHR type resource.
     */
    XHR,

    /**
     * Document type resource.
     */
    DOCUMENT,

    // Common kinds
    /**
     * Native type resource.
     */
    NATIVE,

    /**
     * Image type resource.
     */
    IMAGE,

    /**
     * JS type resource.
     */
    JS,

    /**
     * Font type resource.
     */
    FONT,

    /**
     * CSS type resource.
     */
    CSS,

    /**
     * Media type resource.
     */
    MEDIA,

    /**
     * Other type resource.
     */
    OTHER
}
