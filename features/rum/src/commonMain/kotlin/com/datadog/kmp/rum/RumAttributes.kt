/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum

/**
 * This class holds constant RUM attribute keys.
 */
object RumAttributes {

    /**
     * Specifies a custom error fingerprint for the supplied error.
     */
    const val ERROR_FINGERPRINT: String = "_dd.error.fingerprint"
}
