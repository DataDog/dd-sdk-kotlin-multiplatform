/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log

/**
 * An entry point to Datadog Logs feature.
 */
expect object Logs {

    /**
     * Enables a Logs feature.
     */
    fun enable()

    /**
     * Add a custom attribute to all future logs sent by loggers.
     *
     * Values can be nested up to 10 levels deep. Keys using more than 10 levels will be sanitized by SDK.
     *
     * @param key the key for this attribute
     * @param value the attribute value
     */
    fun addAttribute(key: String, value: Any?)

    /**
     * Remove a custom attribute from all future logs sent by loggers.
     *
     * Previous logs won't lose the attribute value associated with this key if they were created
     * prior to this call.
     *
     * @param key the key of the attribute to remove
     */
    fun removeAttribute(key: String)
}
